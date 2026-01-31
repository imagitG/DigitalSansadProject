package com.digitalSansad.sow.service;

import com.digitalSansad.sow.dto.*;
import com.digitalSansad.sow.entity.*;
import com.digitalSansad.sow.exception.ResourceNotFoundException;
import com.digitalSansad.sow.repository.SowActionRepository;
import com.digitalSansad.sow.repository.SowDocumentRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class SowService {

  @Value("${storage.sow.base-path:storage/sow}")
  private String storageBasePath;

  @Value("${chatbot.service.url}")
  private String chatbotServiceUrl;

  private final SowDocumentRepository documentRepo;
  private final SowActionRepository actionRepo;
  private final RestTemplate restTemplate;

  public SowService(
      SowDocumentRepository documentRepo,
      SowActionRepository actionRepo,
      RestTemplate restTemplate) {
    this.documentRepo = documentRepo;
    this.actionRepo = actionRepo;
    this.restTemplate = restTemplate;
  }

  /* ───────────── Create SOW ───────────── */
  @Transactional
  public SowDocument create(
      MultipartFile file,
      String title,
      UUID userId) throws IOException {

    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("PDF file is required");
    }

    if (!"application/pdf".equalsIgnoreCase(file.getContentType())) {
      throw new IllegalArgumentException("Only PDF files are allowed");
    }

    UUID sowId = UUID.randomUUID();

    Path baseDir = Paths.get(storageBasePath);
    Files.createDirectories(baseDir);

    Path filePath = baseDir.resolve(sowId + ".pdf");
    Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

    System.out.println("\n\nSOW Document about to getter setter: " + "\n\n");

    SowDocument doc = new SowDocument();
    doc.setId(sowId);
    doc.setTitle(title);
    doc.setRefNo("SOW-" + sowId.toString().substring(0, 8).toUpperCase());
    doc.setStatus("IN_PROGRESS");
    doc.setCurrentOwnerRole("SOW_REVIEWER");
    doc.setCreatedBy(userId);
    doc.setFilePath(filePath.toString());
    doc.setFileName(file.getOriginalFilename());
    doc.setContentType(file.getContentType());
    doc.setCreatedAt(Instant.now());

    System.out.println("\n\nSOW Document about to create: " + doc.getId() + "\n\n");

    documentRepo.save(doc);

    System.out.println("\n\nSOW Document Created: " + doc.getId() + "\n\n");

    SowAction action = new SowAction();
    action.setId(UUID.randomUUID());
    action.setSowId(sowId);
    action.setActionType("CREATE");
    action.setActedBy(userId);
    action.setToRole("SOW_REVIEWER");
    action.setActedAt(Instant.now());

    actionRepo.save(action);

    // Send file to chatbot-service for ingestion
    sendFileToChatbotService(file);

    return doc;
  }

  /* ───────────── Fetch File ───────────── */
  public File getFile(UUID id) {
    SowDocument doc = getById(id);
    File file = new File(doc.getFilePath());

    if (!file.exists()) {
      throw new ResourceNotFoundException("PDF not found");
    }
    return file;
  }

  /* ───────────── Fetch Doc ───────────── */
  public SowDocument getById(UUID id) {
    return documentRepo.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("SOW not found: " + id));
  }

  /* ───────────── Metadata ───────────── */
  public SowMetadataResponse metadata(UUID id) {
    SowDocument doc = getById(id);

    SowMetadataResponse res = new SowMetadataResponse();
    res.id = doc.getId();
    res.title = doc.getTitle();
    res.refNo = doc.getRefNo();
    res.status = doc.getStatus();
    res.currentOwnerRole = doc.getCurrentOwnerRole();
    res.createdAt = doc.getCreatedAt();
    res.approvedAt = doc.getApprovedAt();

    return res;
  }

  /* ───────────── Dashboard Tasks ───────────── */
  public List<SowTaskResponse> search(SowSearchRequest req) {

    return documentRepo.findAll().stream()
        .filter(doc -> {
          // Filter by refNo
          if (req.refNo != null && !req.refNo.isEmpty()) {
            if (!doc.getRefNo().toLowerCase().contains(req.refNo.toLowerCase())) {
              return false;
            }
          }

          // Filter by status
          if (req.status != null && !req.status.isEmpty()) {
            if (!doc.getStatus().equals(req.status)) {
              return false;
            }
          }

          // Filter by pendingAt (currentOwnerRole)
          if (req.pendingAt != null && !req.pendingAt.isEmpty()) {
            if (!doc.getCurrentOwnerRole().equals(req.pendingAt)) {
              return false;
            }
          }

          // Filter by createdBy (user ID)
          if (req.createdBy != null) {
            if (!doc.getCreatedBy().equals(req.createdBy)) {
              return false;
            }
          }

          // Filter by creation date
          if (req.createdOn != null) {
            if (doc.getCreatedAt() == null ||
                doc.getCreatedAt()
                    .isBefore(req.createdOn.atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant())
                ||
                doc.getCreatedAt()
                    .isAfter(req.createdOn.plusDays(1).atStartOfDay().atZone(java.time.ZoneId.systemDefault())
                        .toInstant())) {
              return false;
            }
          }

          return true;
        })
        .map(doc -> {
          SowTaskResponse r = new SowTaskResponse();
          r.id = doc.getId();
          r.refNo = doc.getRefNo();
          r.documentName = doc.getTitle();
          r.status = doc.getStatus();
          r.pendingAt = doc.getCurrentOwnerRole();

          return r;
        })
        .toList();
  }

  /* ───────────── Return SOW ───────────── */
  @Transactional
  public SowReturnResponse returnSow(UUID id, SowReturnRequest req, UUID userId) {
    SowDocument doc = getById(id);

    // Store previous role for the action record
    String fromRole = doc.getCurrentOwnerRole();

    // Determine the new owner role based on current role
    String newOwnerRole;
    if ("SOW_REVIEWER".equals(fromRole)) {
      newOwnerRole = "SOW_CREATOR";
    } else if ("SOW_APPROVER".equals(fromRole)) {
      newOwnerRole = "SOW_REVIEWER";
    } else {
      throw new IllegalArgumentException("Cannot return document from role: " + fromRole);
    }

    // Update document status and owner role
    doc.setStatus("RETURNED");
    doc.setCurrentOwnerRole(newOwnerRole);
    documentRepo.save(doc);

    // Create action record
    SowAction action = new SowAction();
    action.setId(UUID.randomUUID());
    action.setSowId(id);
    action.setActionType("RETURN");
    action.setActedBy(userId);
    action.setFromRole(fromRole);
    action.setToRole(newOwnerRole);
    action.setComment(req.comment);
    action.setActedAt(Instant.now());

    actionRepo.save(action);

    return new SowReturnResponse(id, doc.getStatus(), doc.getCurrentOwnerRole());
  }

  /* ───────────── Update File ───────────── */
  @Transactional
  public SowUpdateFileResponse updateFile(UUID id, MultipartFile newFile) throws IOException {
    SowDocument doc = getById(id);

    if (newFile == null || newFile.isEmpty()) {
      throw new IllegalArgumentException("PDF file is required");
    }

    if (!"application/pdf".equalsIgnoreCase(newFile.getContentType())) {
      throw new IllegalArgumentException("Only PDF files are allowed");
    }

    // Delete old file
    if (doc.getFilePath() != null) {
      try {
        Files.deleteIfExists(Paths.get(doc.getFilePath()));
      } catch (IOException e) {
        System.err.println("Failed to delete old file: " + doc.getFilePath());
      }
    }

    // Save new file with same ID
    Path baseDir = Paths.get(storageBasePath);
    Files.createDirectories(baseDir);

    Path filePath = baseDir.resolve(id + ".pdf");
    Files.copy(newFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

    // Update document
    doc.setFilePath(filePath.toString());
    doc.setFileName(newFile.getOriginalFilename());
    doc.setContentType(newFile.getContentType());
    documentRepo.save(doc);

    return new SowUpdateFileResponse(id, doc.getFileName(), doc.getFilePath());
  }

  /* ───────────── Submit SOW ───────────── */
  @Transactional
  public SowSubmitResponse submitSow(UUID id, SowSubmitRequest req, UUID userId, String userRole) {
    SowDocument doc = getById(id);

    // Store current role for the action record
    String fromRole = userRole;

    // Determine the new owner role based on current role
    String newOwnerRole;
    if ("SOW_CREATOR".equals(fromRole)) {
      newOwnerRole = "SOW_REVIEWER";
    } else if ("SOW_REVIEWER".equals(fromRole)) {
      newOwnerRole = "SOW_APPROVER";
    } else {
      throw new IllegalArgumentException("Cannot submit document from role: " + fromRole);
    }

    // Update document status and owner role
    doc.setStatus("IN_PROGRESS");
    doc.setCurrentOwnerRole(newOwnerRole);
    documentRepo.save(doc);

    // Create action record
    SowAction action = new SowAction();
    action.setId(UUID.randomUUID());
    action.setSowId(id);
    action.setActionType("SUBMIT");
    action.setActedBy(userId);
    action.setFromRole(fromRole);
    action.setToRole(newOwnerRole);
    action.setComment("");
    action.setActedAt(Instant.now());

    actionRepo.save(action);

    return new SowSubmitResponse(id, doc.getStatus(), doc.getCurrentOwnerRole());
  }

  /* ───────────── Send File to Chatbot Service ───────────── */
  private void sendFileToChatbotService(MultipartFile file) {
    try {
      String chatbotUploadUrl = chatbotServiceUrl + "/ingest/upload";

      // Create multipart form request using RestTemplate
      org.springframework.util.LinkedMultiValueMap<String, Object> body = new org.springframework.util.LinkedMultiValueMap<>();

      // Wrap the file in a ByteArrayResource
      org.springframework.core.io.ByteArrayResource fileResource = new org.springframework.core.io.ByteArrayResource(
          file.getBytes()) {
        @Override
        public String getFilename() {
          return file.getOriginalFilename();
        }
      };

      body.add("file", fileResource);

      org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
      headers.setContentType(org.springframework.http.MediaType.MULTIPART_FORM_DATA);

      org.springframework.http.HttpEntity<org.springframework.util.LinkedMultiValueMap<String, Object>> requestEntity = new org.springframework.http.HttpEntity<>(
          body, headers);

      restTemplate.postForObject(chatbotUploadUrl, requestEntity, String.class);

      System.out.println("\n\nFile sent to chatbot-service for ingestion: " +
          file.getOriginalFilename() + "\n\n");
    } catch (Exception e) {
      System.err.println("Error sending file to chatbot-service: " + e.getMessage());
      e.printStackTrace();
      // Don't throw exception - file creation should succeed even if chatbot
      // ingestion fails
    }
  }
}
