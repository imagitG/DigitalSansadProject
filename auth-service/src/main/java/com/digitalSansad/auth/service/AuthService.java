package com.digitalSansad.auth.service;

import com.digitalSansad.auth.dto.*;
import com.digitalSansad.auth.entity.Role;
import com.digitalSansad.auth.entity.User;
import com.digitalSansad.auth.repository.RoleRepository;
import com.digitalSansad.auth.repository.UserRepository;
import com.digitalSansad.auth.exception.*;
import org.springframework.stereotype.Service;
import com.digitalSansad.auth.security.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {

  private final UserRepository userRepository;
  private final OtpService otpService;
  private final JWTService jwtService;
  private final RoleRepository roleRepository;

  public AuthService(
      UserRepository userRepository,
      OtpService otpService,
      JWTService jwtService,
      RoleRepository roleRepository) {
    this.userRepository = userRepository;
    this.otpService = otpService;
    this.jwtService = jwtService;
    this.roleRepository = roleRepository;
  }

  /* ---------------- SIGNUP ---------------- */

  public void signup(SignupRequest req) {

    if (userRepository.existsByEmail(req.email())) {
      throw new UserAlreadyExistsException("User with email already exists");
    }

    if (userRepository.existsByMobile(req.mobile())) {
      throw new UserAlreadyExistsException("User with mobile no. already exists");
    }

    User user = new User();
    user.setId(UUID.randomUUID());
    user.setName(req.name());
    user.setDesignation(req.designation());
    user.setEmail(req.email());
    user.setMobile(req.mobile());
    user.setVerified(false);
    user.setCreatedAt(LocalDateTime.now());

    Role defaultRole = roleRepository.findByName("DEFAULT_ROLE")
        .orElseThrow(() -> new RuntimeException("DEFAULT_ROLE missing"));

    user.getRoles().add(defaultRole);

    userRepository.save(user);

    otpService.generateOtp(user);
  }

  /* ---------------- LOGIN ---------------- */

  public void login(LoginRequest req) {

    User user = userRepository.findByEmail(req.email())
        .orElseThrow(() -> new UserNotFoundException("User not found with email: " + req.email()));

    otpService.generateOtp(user);
  }

  /* ---------------- VERIFY OTP ---------------- */

  public String verifyOtp(OtpVerifyRequest req) {

    User user = userRepository.findByEmail(req.email())
        .orElseThrow(() -> new UserNotFoundException("User not found"));

    otpService.verifyOtp(user, req.otp());

    user.setVerified(true);
    userRepository.save(user);

    return jwtService.generateToken(user);
  }

  /* ---------------- RESEND OTP ---------------- */

  public void resendOtp(ResendOtpRequest req) {

    User user = userRepository.findByEmail(req.email())
        .orElseThrow(() -> new UserNotFoundException("User not found with email: " + req.email()));

    otpService.generateOtp(user);
  }

  // public void assignRole(UUID userId, Role role) {
  // User user = userRepository.findById(userId)
  // .orElseThrow(() -> new UserNotFoundException("User not found"));

  // user.setRole(role);
  // userRepository.save(user);
  // }

}
