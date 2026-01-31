package com.digitalSansad.sow.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "sow_documents", schema = "core")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SowDocument {

  @Id
  @Column(nullable = false, updatable = false)
  private UUID id;

  private String title;

  @Column(name = "ref_no")
  private String refNo;

  private String status;

  @Column(name = "current_owner_role")
  private String currentOwnerRole;

  @Column(name = "created_by")
  private UUID createdBy;

  @Column(name = "file_path")
  private String filePath;

  @Column(name = "file_name")
  private String fileName;

  @Column(name = "content_type")
  private String contentType;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt = Instant.now();

  @Column(name = "approved_at")
  private Instant approvedAt;

}
