package com.digitalSansad.auth.controller;

import com.digitalSansad.auth.dto.UpdateRoleRequest;
import com.digitalSansad.auth.dto.UserResponse;
import com.digitalSansad.auth.service.AdminService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

  private final AdminService adminService;

  @GetMapping("/users")
  public List<UserResponse> getUsers() {
    return adminService.getAllUsers();
  }

  @PutMapping("/users/{userId}/roles")
  public ResponseEntity<?> updateUserRoles(
      @PathVariable UUID userId,
      @RequestBody List<String> roles) {

    UpdateRoleRequest req = new UpdateRoleRequest(userId, roles);
    adminService.updateUserRoles(req);
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/users/{userId}/roles/{role}")
  public ResponseEntity<?> removeUserRole(
      @PathVariable UUID userId,
      @PathVariable String role) {

    adminService.removeUserRole(userId, role);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/roles")
  public List<String> getRoles() {
    return adminService.getAllRoles();
  }

}
