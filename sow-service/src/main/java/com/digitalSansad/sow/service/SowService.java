package com.digitalSansad.sow.service;

import com.digitalSansad.sow.dto.*;
import com.digitalSansad.sow.entity.*;
import com.digitalSansad.sow.exception.ResourceNotFoundException;
import com.digitalSansad.sow.exception.FileValidationException;
import com.digitalSansad.sow.exception.ExternalServiceException;
import com.digitalSansad.sow.exception.R2StorageException;
import com.digitalSansad.sow.repository.SowActionRepository;
import com.digitalSansad.sow.repository.SowDocumentRepository;
import com.digitalSansad.sow.service.R2StorageService;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class SowService {

  private static final Logger logger = LoggerFactory.getLogger(SowService.class);

  @Value("${chatbot.service.url}")
  private String chatbotServiceUrl;

  private final SowDocumentRepository documentRepo;
  private final SowActionRepository actionRepo;
  private final RestTemplate restTemplate;
  private final R2StorageService r2StorageService;

  public SowService(
      SowDocumentRepository documentRepo,
      SowActionRepository actionRepo,
      RestTemplate restTemplate, R2StorageService r2StorageService) {
    this.documentRepo = documentRepo;
    this.actionRepo = actionRepo;
    this.restTemplate = restTemplate;
    this.r2StorageService = r2StorageService;
    logger.info("SowService initialized");
  }

  /* ───────────── Create SOW ───────────── */
  @Transactional
  public SowDocument create(
      MultipartFile file,
      String title,
      UUID userId) throws IOException {

    logger.info("Creating SOW document - Title: {}, User ID: {}", title, userId);

    try {
      if (file == null || file.isEmpty()) {
        logger.error("File validation failed: File is null or empty");
        throw new FileValidationException("PDF file is required");
      }

      if (!("application/pdf".equalsIgnoreCase(file.getContentType()))) {
        logger.error("File validation failed: Invalid content type - {}", file.getContentType());
        throw new FileValidationException("Only PDF files are allowed");
      }

      if (file.getSize() > 10_000_000) {
        logger.error("File validation failed: File too large - {} bytes", file.getSize());
        throw new FileValidationException("File too large (max 10MB)");
      }

      if (title == null || title.trim().isEmpty()) {
        logger.error("Validation failed: Title is null or empty");
        throw new IllegalArgumentException("SOW title is required");
      }

      if (userId == null) {
        logger.error("Validation failed: User ID is null");
        throw new IllegalArgumentException("User ID is required");
      }

      UUID sowId = UUID.randomUUID();
      logger.debug("Generated SOW ID: {}", sowId);

      String key = "sow/" + sowId + ".pdf";
      logger.debug("Uploading file to R2 with key: {}", key);

      r2StorageService.upload(file, key);
      logger.info("File uploaded to R2 successfully");

      SowDocument doc = new SowDocument();
      doc.setId(sowId);
      doc.setTitle(title);
      doc.setRefNo("SOW-" + sowId.toString().substring(0, 8).toUpperCase());
      doc.setStatus("IN_PROGRESS");
      doc.setCurrentOwnerRole("SOW_REVIEWER");
      doc.setCreatedBy(userId);
      doc.setFilePath(key);
      doc.setFileName(file.getOriginalFilename());
      doc.setContentType(file.getContentType());
      doc.setCreatedAt(Instant.now());

      logger.debug("Saving SOW document to database - ID: {}", sowId);
      documentRepo.save(doc);
      logger.info("SOW document saved successfully - ID: {}", sowId);

      SowAction action = new SowAction();
      action.setId(UUID.randomUUID());
      action.setSowId(sowId);
      action.setActionType("CREATE");
      action.setActedBy(userId);
      action.setToRole("SOW_REVIEWER");
      action.setActedAt(Instant.now());

      logger.debug("Creating action record for SOW creation");
      actionRepo.save(action);

      logger.info("Sending file to chatbot service for ingestion");
      sendFileToChatbotService(file);

      logger.info("SOW document creation completed successfully - ID: {}", sowId);
      return doc;

    } catch (FileValidationException e) {
      logger.error("File validation error during SOW creation: {}", e.getMessage());
      throw e;
    } catch (R2StorageException e) {
      logger.error("R2 storage error during SOW creation: {}", e.getMessage());
      throw new ExternalServiceException("Failed to upload file to storage: " + e.getMessage(), e);
    } catch (IOException e) {
      logger.error("IO error during SOW creation: {}", e.getMessage(), e);
      throw new FileValidationException("Error reading file: " + e.getMessage(), e);
    } catch (Exception e) {
      logger.error("Unexpected error during SOW creation: {}", e.getMessage(), e);
      throw new RuntimeException("Error creating SOW document: " + e.getMessage(), e);
    }
  }

  /* ───────────── Fetch File ───────────── */
  public ResponseInputStream<GetObjectResponse> getFile(UUID id) {
    logger.info("Fetching file for SOW ID: {}", id);

    try {
      if (id == null) {
        logger.error("File fetch failed: SOW ID is null");
        throw new IllegalArgumentException("SOW ID is required");
      }

      logger.debug("Retrieving SOW document from database - ID: {}", id);
      SowDocument doc = getById(id);

      logger.debug("Downloading file from R2 - Path: {}", doc.getFilePath());
      ResponseInputStream<GetObjectResponse> stream = r2StorageService.download(doc.getFilePath());
      logger.info("File downloaded successfully from R2 - ID: {}", id);

      return stream;

    } catch (ResourceNotFoundException e) {
      logger.error("SOW document not found: {}", e.getMessage());
      throw e;
    } catch (R2StorageException e) {
      logger.error("R2 storage error while fetching file - ID: {}, Error: {}", id, e.getMessage());
      throw new ResourceNotFoundException("File not found in storage");
    } catch (Exception e) {
      logger.error("Unexpected error while fetching file - ID: {}, Error: {}", id, e.getMessage(), e);
      throw new ResourceNotFoundException("Error retrieving file: " + e.getMessage());
    }
  }
  // public File getFile(UUID id) {
  // SowDocument doc = getById(id);
  // File file = new File(doc.getFilePath());

  // if (!file.exists()) {
  // throw new ResourceNotFoundException("PDF not found");
  // }
  // return file;
  // }

  /* ───────────── Fetch Doc ───────────── */
  public SowDocument getById(UUID id) {
    logger.debug("Retrieving SOW document by ID: {}", id);

    if (id == null) {
      logger.error("Retrieve failed: SOW ID is null");
      throw new IllegalArgumentException("SOW ID is required");
    }

    try {
      SowDocument doc = documentRepo.findById(id)
          .orElseThrow(() -> {
            logger.error("SOW document not found - ID: {}", id);
            return new ResourceNotFoundException("SOW not found: " + id);
          });

      logger.debug("SOW document retrieved successfully - ID: {}", id);
      return doc;

    } catch (ResourceNotFoundException e) {
      logger.error("SOW resource not found: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      logger.error("Unexpected error retrieving SOW - ID: {}, Error: {}", id, e.getMessage(), e);
      throw new RuntimeException("Error retrieving SOW document: " + e.getMessage(), e);
    }
  }

  /* ───────────── Metadata ───────────── */
  public SowMetadataResponse metadata(UUID id) {
    logger.info("Fetching metadata for SOW ID: {}", id);

    try {
      if (id == null) {
        logger.error("Metadata fetch failed: SOW ID is null");
        throw new IllegalArgumentException("SOW ID is required");
      }

      logger.debug("Retrieving SOW document for metadata - ID: {}", id);
      SowDocument doc = getById(id);

      SowMetadataResponse res = new SowMetadataResponse();
      res.id = doc.getId();
      res.title = doc.getTitle();
      res.refNo = doc.getRefNo();
      res.status = doc.getStatus();
      res.currentOwnerRole = doc.getCurrentOwnerRole();
      res.createdAt = doc.getCreatedAt();
      res.approvedAt = doc.getApprovedAt();

      logger.info("Metadata retrieved successfully for SOW ID: {}", id);
      return res;

    } catch (ResourceNotFoundException e) {
      logger.error("SOW not found for metadata request: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      logger.error("Unexpected error retrieving metadata - ID: {}, Error: {}", id, e.getMessage(), e);
      throw new RuntimeException("Error retrieving SOW metadata: " + e.getMessage(), e);
    }
  }

  /* ───────────── Dashboard Tasks ───────────── */
  public List<SowTaskResponse> search(SowSearchRequest req) {
    logger.info(
        "Executing SOW search with filters - refNo: {}, status: {}, pendingAt: {}, createdBy: {}, createdOn: {}",
        req.refNo, req.status, req.pendingAt, req.createdBy, req.createdOn);

    try {
      if (req == null) {
        logger.error("Search failed: Search request is null");
        throw new IllegalArgumentException("Search request is required");
      }

      logger.debug("Fetching all SOW documents from database");
      List<SowTaskResponse> results = documentRepo.findAll().stream()
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

      logger.info("Search completed successfully - Found {} results", results.size());
      return results;

    } catch (Exception e) {
      logger.error("Unexpected error during SOW search: {}", e.getMessage(), e);
      throw new RuntimeException("Error searching SOW documents: " + e.getMessage(), e);
    }
  }

  /* ───────────── Return SOW ───────────── */
  @Transactional
  public SowReturnResponse returnSow(UUID id, SowReturnRequest req, UUID userId) {
    logger.info("Processing SOW return - SOW ID: {}, User ID: {}", id, userId);

    try {
      if (id == null) {
        logger.error("Return failed: SOW ID is null");
        throw new IllegalArgumentException("SOW ID is required");
      }

      if (userId == null) {
        logger.error("Return failed: User ID is null");
        throw new IllegalArgumentException("User ID is required");
      }

      logger.debug("Retrieving SOW document for return - ID: {}", id);
      SowDocument doc = getById(id);

      String fromRole = doc.getCurrentOwnerRole();
      logger.debug("Current owner role: {}", fromRole);

      String newOwnerRole;
      if ("SOW_REVIEWER".equals(fromRole)) {
        newOwnerRole = "SOW_CREATOR";
      } else if ("SOW_APPROVER".equals(fromRole)) {
        newOwnerRole = "SOW_REVIEWER";
      } else {
        logger.error("Invalid return state for SOW ID: {} - Current role: {}", id, fromRole);
        throw new IllegalArgumentException("Cannot return document from role: " + fromRole);
      }

      logger.debug("Updating SOW status to RETURNED and owner role to: {}", newOwnerRole);
      doc.setStatus("RETURNED");
      doc.setCurrentOwnerRole(newOwnerRole);
      documentRepo.save(doc);

      SowAction action = new SowAction();
      action.setId(UUID.randomUUID());
      action.setSowId(id);
      action.setActionType("RETURN");
      action.setActedBy(userId);
      action.setFromRole(fromRole);
      action.setToRole(newOwnerRole);
      action.setComment(req.comment);
      action.setActedAt(Instant.now());

      logger.debug("Creating action record for SOW return");
      actionRepo.save(action);

      logger.info("SOW return processed successfully - ID: {}", id);
      return new SowReturnResponse(id, doc.getStatus(), doc.getCurrentOwnerRole());

    } catch (IllegalArgumentException e) {
      logger.error("Validation error during SOW return: {}", e.getMessage());
      throw e;
    } catch (ResourceNotFoundException e) {
      logger.error("SOW not found for return: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      logger.error("Unexpected error during SOW return - ID: {}, Error: {}", id, e.getMessage(), e);
      throw new RuntimeException("Error processing SOW return: " + e.getMessage(), e);
    }
  }

  /* ───────────── Update File ───────────── */
  @Transactional
  public SowUpdateFileResponse updateFile(UUID id, MultipartFile newFile) throws IOException {
    logger.info("Updating file for SOW ID: {}", id);

    try {
      if (id == null) {
        logger.error("Update failed: SOW ID is null");
        throw new IllegalArgumentException("SOW ID is required");
      }

      logger.debug("Validating new file");
      if (newFile == null || newFile.isEmpty()) {
        logger.error("File validation failed: New file is null or empty");
        throw new FileValidationException("PDF file is required");
      }

      if (!("application/pdf".equalsIgnoreCase(newFile.getContentType()))) {
        logger.error("File validation failed: Invalid content type - {}", newFile.getContentType());
        throw new FileValidationException("Only PDF files are allowed");
      }

      logger.debug("Retrieving SOW document for file update - ID: {}", id);
      SowDocument doc = getById(id);

      String key = doc.getFilePath();
      logger.debug("Current file path: {}", key);

      logger.debug("Deleting old file from R2");
      r2StorageService.delete(key);

      logger.debug("Uploading new file to R2");
      r2StorageService.upload(newFile, key);

      doc.setFileName(newFile.getOriginalFilename());
      doc.setContentType(newFile.getContentType());

      logger.debug("Saving updated SOW document");
      documentRepo.save(doc);

      logger.info("File updated successfully for SOW ID: {}", id);
      return new SowUpdateFileResponse(id, doc.getFileName(), key);

    } catch (FileValidationException e) {
      logger.error("File validation error during update: {}", e.getMessage());
      throw e;
    } catch (R2StorageException e) {
      logger.error("R2 storage error during file update - ID: {}, Error: {}", id, e.getMessage());
      throw new ExternalServiceException("Failed to update file in storage: " + e.getMessage(), e);
    } catch (ResourceNotFoundException e) {
      logger.error("SOW not found for file update: {}", e.getMessage());
      throw e;
    } catch (IOException e) {
      logger.error("IO error during file update - ID: {}, Error: {}", id, e.getMessage(), e);
      throw new FileValidationException("Error reading new file: " + e.getMessage(), e);
    } catch (Exception e) {
      logger.error("Unexpected error during file update - ID: {}, Error: {}", id, e.getMessage(), e);
      throw new RuntimeException("Error updating SOW file: " + e.getMessage(), e);
    }
  }
  // @Transactional
  // public SowUpdateFileResponse updateFile(UUID id, MultipartFile newFile)
  // throws IOException {
  // SowDocument doc = getById(id);

  // if (newFile == null || newFile.isEmpty()) {
  // throw new IllegalArgumentException("PDF file is required");
  // }

  // if (!"application/pdf".equalsIgnoreCase(newFile.getContentType())) {
  // throw new IllegalArgumentException("Only PDF files are allowed");
  // }

  // // Delete old file
  // if (doc.getFilePath() != null) {
  // try {
  // Files.deleteIfExists(Paths.get(doc.getFilePath()));
  // } catch (IOException e) {
  // System.err.println("Failed to delete old file: " + doc.getFilePath());
  // }
  // }

  // // Save new file with same ID
  // Path baseDir = Paths.get(storageBasePath);
  // Files.createDirectories(baseDir);

  // Path filePath = baseDir.resolve(id + ".pdf");
  // Files.copy(newFile.getInputStream(), filePath,
  // StandardCopyOption.REPLACE_EXISTING);

  // // Update document
  // doc.setFilePath(filePath.toString());
  // doc.setFileName(newFile.getOriginalFilename());
  // doc.setContentType(newFile.getContentType());
  // documentRepo.save(doc);

  // return new SowUpdateFileResponse(id, doc.getFileName(), doc.getFilePath());
  // }

  /* ───────────── Submit SOW ───────────── */
  @Transactional
  public SowSubmitResponse submitSow(UUID id, SowSubmitRequest req, UUID userId, String userRole) {
    logger.info("Submitting SOW - SOW ID: {}, User ID: {}, User Role: {}", id, userId, userRole);

    try {
      if (id == null) {
        logger.error("Submit failed: SOW ID is null");
        throw new IllegalArgumentException("SOW ID is required");
      }

      if (userId == null) {
        logger.error("Submit failed: User ID is null");
        throw new IllegalArgumentException("User ID is required");
      }

      if (userRole == null || userRole.isEmpty()) {
        logger.error("Submit failed: User role is null or empty");
        throw new IllegalArgumentException("User role is required");
      }

      logger.debug("Retrieving SOW document for submission - ID: {}", id);
      SowDocument doc = getById(id);

      String fromRole = userRole;
      logger.debug("Submission from role: {}", fromRole);

      String newOwnerRole;
      if ("SOW_CREATOR".equals(fromRole)) {
        newOwnerRole = "SOW_REVIEWER";
      } else if ("SOW_REVIEWER".equals(fromRole)) {
        newOwnerRole = "SOW_APPROVER";
      } else {
        logger.error("Invalid submit state for SOW ID: {} - Current role: {}", id, fromRole);
        throw new IllegalArgumentException("Cannot submit document from role: " + fromRole);
      }

      logger.debug("Updating SOW status to IN_PROGRESS and owner role to: {}", newOwnerRole);
      doc.setStatus("IN_PROGRESS");
      doc.setCurrentOwnerRole(newOwnerRole);
      documentRepo.save(doc);

      SowAction action = new SowAction();
      action.setId(UUID.randomUUID());
      action.setSowId(id);
      action.setActionType("SUBMIT");
      action.setActedBy(userId);
      action.setFromRole(fromRole);
      action.setToRole(newOwnerRole);
      action.setComment("");
      action.setActedAt(Instant.now());

      logger.debug("Creating action record for SOW submission");
      actionRepo.save(action);

      logger.info("SOW submission processed successfully - ID: {}", id);
      return new SowSubmitResponse(id, doc.getStatus(), doc.getCurrentOwnerRole());

    } catch (IllegalArgumentException e) {
      logger.error("Validation error during SOW submission: {}", e.getMessage());
      throw e;
    } catch (ResourceNotFoundException e) {
      logger.error("SOW not found for submission: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      logger.error("Unexpected error during SOW submission - ID: {}, Error: {}", id, e.getMessage(), e);
      throw new RuntimeException("Error processing SOW submission: " + e.getMessage(), e);
    }
  }

  /* ───────────── Send File to Chatbot Service ───────────── */
  private void sendFileToChatbotService(MultipartFile file) {
    logger.info("Sending file to chatbot service for ingestion - File: {}", file.getOriginalFilename());

    try {
      if (file == null || file.isEmpty()) {
        logger.warn("Skipping chatbot ingestion: File is null or empty");
        return;
      }

      String chatbotUploadUrl = chatbotServiceUrl + "/ingest/upload";

      if (chatbotServiceUrl == null || chatbotServiceUrl.isEmpty()) {
        logger.warn("Chatbot service URL not configured, skipping ingestion");
        return;
      }

      logger.debug("Chatbot service URL: {}", chatbotUploadUrl);

      org.springframework.util.LinkedMultiValueMap<String, Object> body = new org.springframework.util.LinkedMultiValueMap<>();

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

      logger.debug("Posting file to chatbot service");
      restTemplate.postForObject(chatbotUploadUrl, requestEntity, String.class);

      logger.info("File sent to chatbot service successfully - File: {}", file.getOriginalFilename());

    } catch (RestClientException e) {
      logger.warn("Chatbot service communication error (non-fatal): {} - File ingestion skipped", e.getMessage());
      logger.debug("Chatbot service error details", e);
    } catch (IOException e) {
      logger.warn("IO error reading file for chatbot service (non-fatal): {} - File ingestion skipped", e.getMessage());
      logger.debug("IO error details", e);
    } catch (Exception e) {
      logger.warn("Unexpected error sending file to chatbot service (non-fatal): {} - File ingestion skipped",
          e.getMessage());
      logger.debug("Unexpected error details", e);
    }
  }
}
