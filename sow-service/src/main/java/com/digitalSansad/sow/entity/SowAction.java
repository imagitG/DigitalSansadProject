package com.digitalSansad.sow.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "sow_actions", schema = "core")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class SowAction {

  @Id
  private UUID id;

  @Column(name = "sow_id")
  private UUID sowId;

  @Column(name = "action_type")
  private String actionType;

  @Column(name = "acted_by")
  private UUID actedBy;

  @Column(name = "from_role")
  private String fromRole;

  @Column(name = "to_role")
  private String toRole;

  private String comment;

  @Column(name = "acted_at")
  private Instant actedAt = Instant.now();
}
