package com.digitalSansad.sow.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import com.digitalSansad.sow.dto.*;
import com.digitalSansad.sow.entity.SowDocument;
import com.digitalSansad.sow.service.SowService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/sow")
public class SowController {

  private static final Logger logger = LoggerFactory.getLogger(SowController.class);

  private final SowService service;

  public SowController(SowService service) {
    this.service = service;
  }

  /* ───────────── Create SOW ───────────── */
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<SowCreateResponse> createSow(
      @RequestParam("file") MultipartFile file,
      @RequestParam("title") String title,
      Authentication authentication) throws IOException {

    logger.info("=== POST /sow: Create SOW endpoint called ===");
    logger.debug("File: {}, Title: {}, User: {}", file.getOriginalFilename(), title,
        authentication != null ? authentication.getName() : "UNKNOWN");

    if (authentication == null || authentication.getName() == null) {
      logger.error("Authentication is null or missing user name");
      throw new IllegalArgumentException("User authentication is required");
    }

    UUID userId;
    try {
      userId = UUID.fromString(authentication.getName());
      logger.debug("Parsed user ID: {}", userId);
    } catch (IllegalArgumentException ex) {
      logger.error("Invalid user ID format. Expected UUID but got: {}", authentication.getName());
      throw new IllegalArgumentException(
          "Invalid user ID format in authentication. Expected UUID but got: " + authentication.getName());
    }

    logger.info("Creating SOW document for user: {} with title: {}", userId, title);
    SowDocument doc = service.create(file, title, userId);
    logger.info("SOW document created successfully with ID: {}", doc.getId());

    return ResponseEntity.ok(new SowCreateResponse(doc.getId()));
  }

  /* ───────────── Search Tasks ───────────── */
  @PostMapping("/tasks/search")
  public List<SowTaskResponse> search(
      @RequestBody SowSearchRequest req) {
    logger.info("=== POST /sow/tasks/search: Search tasks endpoint called ===");
    logger.debug("Search request - refNo: {}, status: {}, pendingAt: {}, createdBy: {}, createdOn: {}",
        req.refNo, req.status, req.pendingAt, req.createdBy, req.createdOn);
    List<SowTaskResponse> results = service.search(req);
    logger.info("Search returned {} results", results.size());
    return results;
  }

  /* ───────────── Download PDF ───────────── */
  @GetMapping("/{id}/file")
  public ResponseEntity<InputStreamResource> getFile(@PathVariable UUID id) {
    logger.info("=== GET /sow/{}/file: Download file endpoint called ===", id);
    var stream = service.getFile(id);
    logger.info("File stream retrieved successfully for SOW ID: {}", id);

    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_PDF)
        .body(new InputStreamResource(stream));
  }
  // @GetMapping("/{id}/file")
  // public ResponseEntity<Resource> getFile(@PathVariable UUID id) throws
  // IOException {

  // File file = service.getFile(id);
  // SowDocument doc = service.getById(id);

  // return ResponseEntity.ok()
  // .contentType(MediaType.APPLICATION_PDF)
  // .header(HttpHeaders.CONTENT_DISPOSITION,
  // "inline; filename=\"" + doc.getFileName() + "\"")
  // .body(new FileSystemResource(file));
  // }

  /* ───────────── Metadata ───────────── */
  @GetMapping("/{id}/metadata")
  public SowMetadataResponse metadata(@PathVariable UUID id) {
    logger.info("=== GET /sow/{}/metadata: Metadata endpoint called ===", id);
    SowMetadataResponse result = service.metadata(id);
    logger.info("Metadata retrieved for SOW ID: {}", id);
    return result;
  }

  /* ───────────── Return SOW ───────────── */
  @PostMapping("/{id}/return")
  public ResponseEntity<SowReturnResponse> returnSow(
      @PathVariable UUID id,
      @RequestBody SowReturnRequest req,
      Authentication authentication) {
    logger.info("=== POST /sow/{}/return: Return SOW endpoint called ===", id);

    if (authentication == null || authentication.getName() == null) {
      logger.error("Authentication is null or missing user name for return SOW");
      throw new IllegalArgumentException("User authentication is required");
    }

    UUID userId;
    try {
      userId = UUID.fromString(authentication.getName());
      logger.debug("Parsed user ID for return: {}", userId);
    } catch (IllegalArgumentException ex) {
      logger.error("Invalid user ID format for return. Expected UUID but got: {}", authentication.getName());
      throw new IllegalArgumentException(
          "Invalid user ID format in authentication. Expected UUID but got: " + authentication.getName());
    }

    logger.info("Returning SOW ID: {} by user: {} with comment: {}", id, userId, req.comment);
    SowReturnResponse response = service.returnSow(id, req, userId);
    logger.info("SOW return processed successfully");
    return ResponseEntity.ok(response);
  }

  /* ───────────── Update File ───────────── */
  @PostMapping("/{id}/update-file")
  public ResponseEntity<SowUpdateFileResponse> updateFile(
      @PathVariable UUID id,
      @RequestParam("file") MultipartFile file) throws IOException {
    logger.info("=== POST /sow/{}/update-file: Update file endpoint called ===", id);
    logger.debug("New file: {}", file.getOriginalFilename());

    SowUpdateFileResponse response = service.updateFile(id, file);
    logger.info("SOW file updated successfully for ID: {}", id);
    return ResponseEntity.ok(response);
  }

  /* ───────────── Submit SOW ───────────── */
  @PostMapping("/{id}/submit")
  public ResponseEntity<SowSubmitResponse> submitSow(
      @PathVariable UUID id,
      @RequestBody SowSubmitRequest req,
      Authentication authentication) {
    logger.info("=== POST /sow/{}/submit: Submit SOW endpoint called ===", id);

    if (authentication == null || authentication.getName() == null) {
      logger.error("Authentication is null or missing user name for submit SOW");
      throw new IllegalArgumentException("User authentication is required");
    }

    UUID userId;
    String userRole = null;

    try {
      userId = UUID.fromString(authentication.getName());
      logger.debug("Parsed user ID for submit: {}", userId);
    } catch (IllegalArgumentException ex) {
      logger.error("Invalid user ID format for submit. Expected UUID but got: {}", authentication.getName());
      throw new IllegalArgumentException(
          "Invalid user ID format in authentication. Expected UUID but got: " + authentication.getName());
    }

    // Extract user role from authorities
    String authorities = authentication.getAuthorities().toString();
    logger.debug("User authorities: {}", authorities);

    if (authorities.contains("SOW_CREATOR")) {
      userRole = "SOW_CREATOR";
    } else if (authorities.contains("SOW_REVIEWER")) {
      userRole = "SOW_REVIEWER";
    } else if (authorities.contains("SOW_APPROVER")) {
      userRole = "SOW_APPROVER";
    } else {
      logger.error("User has no valid SOW role. Authorities: {}", authorities);
      throw new IllegalArgumentException("User must have a valid SOW role");
    }

    logger.info("Submitting SOW ID: {} by user: {} with role: {}", id, userId, userRole);
    SowSubmitResponse response = service.submitSow(id, req, userId, userRole);
    logger.info("SOW submission processed successfully");
    return ResponseEntity.ok(response);
  }
}
