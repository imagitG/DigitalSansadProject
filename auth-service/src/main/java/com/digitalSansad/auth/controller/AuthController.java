package com.digitalSansad.auth.controller;

import com.digitalSansad.auth.dto.*;
import com.digitalSansad.auth.entity.Role;
import com.digitalSansad.auth.service.AuthService;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/auth")
public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @GetMapping("/health") // auth/health
  public String health() {
    return "OK";
  }

  @PostMapping("/signup")
  public ResponseEntity<AuthResponse> signup(
      @RequestBody SignupRequest req) {

    authService.signup(req);
    return ResponseEntity.ok(
        new AuthResponse("OTP sent", 120, null));
  }

  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(
      @RequestBody LoginRequest req) {

    authService.login(req);
    return ResponseEntity.ok(
        new AuthResponse("OTP sent", 120, null));
  }

  @PostMapping("/verify-otp")
  public ResponseEntity<AuthResponse> verifyOtp(
      @RequestBody OtpVerifyRequest req) {

    String token = authService.verifyOtp(req);

    return ResponseEntity.ok(
        new AuthResponse(
            "Login successful",
            3600, // token expiry in seconds
            token));
  }

  @PostMapping("/resend-otp")
  public ResponseEntity<AuthResponse> resendOtp(
      @RequestBody ResendOtpRequest req) {

    authService.resendOtp(req);
    return ResponseEntity.ok(
        new AuthResponse("OTP resent", 120, null));
  }

  @PostMapping("/logout")
  public ResponseEntity<AuthResponse> logout() {
    // JWT is stateless, so logout on backend just acknowledges the request
    // The frontend clears the token from localStorage
    return ResponseEntity.ok(
        new AuthResponse("Logout successful", 0, null));
  }

  // @PutMapping("/admin/users/{id}/role")
  // @PreAuthorize("hasRole('ADMIN')")
  // public void assignRole(
  // @PathVariable UUID id,
  // @RequestParam Role role) {

  // authService.assignRole(id, role);
  // }

}
