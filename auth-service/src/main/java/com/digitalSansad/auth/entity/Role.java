package com.digitalSansad.auth.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "roles", schema = "core")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Role {

  @Id
  private UUID id;

  @Column(nullable = false, unique = true)
  private String name;
}
