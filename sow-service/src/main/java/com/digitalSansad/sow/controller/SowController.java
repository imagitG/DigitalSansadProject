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
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/sow")
public class SowController {

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

    if (authentication == null || authentication.getName() == null) {
      throw new IllegalArgumentException("User authentication is required");
    }

    UUID userId;
    try {
      userId = UUID.fromString(authentication.getName());
    } catch (IllegalArgumentException ex) {
      // If getName() returns email instead of UUID, we need to handle this
      // For now, throw a clear error
      throw new IllegalArgumentException(
          "Invalid user ID format in authentication. Expected UUID but got: " + authentication.getName());
    }

    SowDocument doc = service.create(file, title, userId);

    return ResponseEntity.ok(new SowCreateResponse(doc.getId()));
  }

  /* ───────────── Search Tasks ───────────── */
  @PostMapping("/tasks/search")
  public List<SowTaskResponse> search(
      @RequestBody SowSearchRequest req) {
    return service.search(req);
  }

  /* ───────────── Download PDF ───────────── */
  @GetMapping("/{id}/file")
  public ResponseEntity<Resource> getFile(@PathVariable UUID id) throws IOException {

    File file = service.getFile(id);
    SowDocument doc = service.getById(id);

    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_PDF)
        .header(HttpHeaders.CONTENT_DISPOSITION,
            "inline; filename=\"" + doc.getFileName() + "\"")
        .body(new FileSystemResource(file));
  }

  /* ───────────── Metadata ───────────── */
  @GetMapping("/{id}/metadata")
  public SowMetadataResponse metadata(@PathVariable UUID id) {
    return service.metadata(id);
  }

  /* ───────────── Return SOW ───────────── */
  @PostMapping("/{id}/return")
  public ResponseEntity<SowReturnResponse> returnSow(
      @PathVariable UUID id,
      @RequestBody SowReturnRequest req,
      Authentication authentication) {

    if (authentication == null || authentication.getName() == null) {
      throw new IllegalArgumentException("User authentication is required");
    }

    UUID userId;
    try {
      userId = UUID.fromString(authentication.getName());
    } catch (IllegalArgumentException ex) {
      throw new IllegalArgumentException(
          "Invalid user ID format in authentication. Expected UUID but got: " + authentication.getName());
    }

    SowReturnResponse response = service.returnSow(id, req, userId);
    return ResponseEntity.ok(response);
  }

  /* ───────────── Update File ───────────── */
  @PostMapping("/{id}/update-file")
  public ResponseEntity<SowUpdateFileResponse> updateFile(
      @PathVariable UUID id,
      @RequestParam("file") MultipartFile file) throws IOException {

    SowUpdateFileResponse response = service.updateFile(id, file);
    return ResponseEntity.ok(response);
  }

  /* ───────────── Submit SOW ───────────── */
  @PostMapping("/{id}/submit")
  public ResponseEntity<SowSubmitResponse> submitSow(
      @PathVariable UUID id,
      @RequestBody SowSubmitRequest req,
      Authentication authentication) {

    if (authentication == null || authentication.getName() == null) {
      throw new IllegalArgumentException("User authentication is required");
    }

    UUID userId;
    String userRole = null;

    try {
      userId = UUID.fromString(authentication.getName());
    } catch (IllegalArgumentException ex) {
      throw new IllegalArgumentException(
          "Invalid user ID format in authentication. Expected UUID but got: " + authentication.getName());
    }

    // Extract user role from authorities
    String authorities = authentication.getAuthorities().toString();
    if (authorities.contains("SOW_CREATOR")) {
      userRole = "SOW_CREATOR";
    } else if (authorities.contains("SOW_REVIEWER")) {
      userRole = "SOW_REVIEWER";
    } else if (authorities.contains("SOW_APPROVER")) {
      userRole = "SOW_APPROVER";
    } else {
      throw new IllegalArgumentException("User must have a valid SOW role");
    }

    SowSubmitResponse response = service.submitSow(id, req, userId, userRole);
    return ResponseEntity.ok(response);
  }
}
