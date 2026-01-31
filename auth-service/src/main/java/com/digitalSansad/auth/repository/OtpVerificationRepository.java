package com.digitalSansad.auth.repository;

import com.digitalSansad.auth.entity.OtpVerification;
import com.digitalSansad.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface OtpVerificationRepository
    extends JpaRepository<OtpVerification, UUID> {

  @Query("""
          SELECT o FROM OtpVerification o
          WHERE o.user = :user
            AND o.used = false
          ORDER BY o.createdAt DESC
      """)
  Optional<OtpVerification> findLatestUnusedOtp(User user);

  @Modifying
  @Query("""
          UPDATE OtpVerification o
          SET o.used = true
          WHERE o.user = :user
            AND o.used = false
      """)
  void invalidateAllUnusedOtps(User user);

}
