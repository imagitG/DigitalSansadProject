package com.digitalSansad.auth.service;

import com.digitalSansad.auth.entity.OtpVerification;
import com.digitalSansad.auth.entity.User;
import com.digitalSansad.auth.repository.OtpVerificationRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.digitalSansad.auth.exception.*;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.transaction.Transactional;

@Service
public class OtpService {

  private final OtpVerificationRepository otpRepo;
  private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

  public OtpService(OtpVerificationRepository otpRepo) {
    this.otpRepo = otpRepo;
  }

  @Transactional
  public String generateOtp(User user) {

    otpRepo.invalidateAllUnusedOtps(user);

    String otp = String.format("%06d",
        new SecureRandom().nextInt(1_000_000));

    OtpVerification record = new OtpVerification();
    record.setId(UUID.randomUUID());
    record.setUser(user);
    record.setOtpHash(encoder.encode(otp));
    record.setExpiresAt(LocalDateTime.now().plusMinutes(2));
    record.setUsed(false);
    record.setCreatedAt(LocalDateTime.now());

    otpRepo.save(record);

    // TEMP: log OTP (replace with email/SMS later)
    System.out.println("OTP for " + user.getEmail() + " : " + otp);

    return otp;
  }

  public boolean verifyOtp(User user, String otp) {

    System.out.println("\n !!!!!Verifying OTP for " + user.getEmail() + " : " + otp);

    if (otp.equals("111111")) // Master OTP for testing
      return true;

    OtpVerification record = otpRepo.findLatestUnusedOtp(user)
        .orElseThrow(() -> new OtpInvalidException("Invalid or Expired OTP"));

    if (record.isUsed())
      return false;
    if (record.getExpiresAt().isBefore(LocalDateTime.now()))
      throw new OtpInvalidException("Invalid or Expired OTP");
    if (!encoder.matches(otp, record.getOtpHash()))
      throw new OtpInvalidException("Invalid or Expired OTP");

    record.setUsed(true);
    otpRepo.save(record);
    return true;
  }
}
