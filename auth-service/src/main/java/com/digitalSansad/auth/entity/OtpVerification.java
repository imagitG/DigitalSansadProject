package com.digitalSansad.auth.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "otp_verification", schema = "core")
@Getter
@Setter
public class OtpVerification {

  @Id
  private UUID id;

  @ManyToOne
  @JoinColumn(name = "user_id")
  private User user;

  @Column(name = "otp_hash")
  private String otpHash;

  @Column(name = "expires_at")
  private LocalDateTime expiresAt;

  private boolean used;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  // getters & setters
}
