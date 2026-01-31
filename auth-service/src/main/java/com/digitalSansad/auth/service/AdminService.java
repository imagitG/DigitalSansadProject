package com.digitalSansad.auth.service;

import com.digitalSansad.auth.dto.UpdateRoleRequest;
import com.digitalSansad.auth.dto.UserResponse;
import com.digitalSansad.auth.entity.Role;
import com.digitalSansad.auth.entity.User;
import com.digitalSansad.auth.repository.RoleRepository;
import com.digitalSansad.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.digitalSansad.auth.exception.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

        private final UserRepository userRepository;
        private final RoleRepository roleRepository;

        public List<UserResponse> getAllUsers() {
                return userRepository.findAll().stream()
                                .map(u -> new UserResponse(
                                                u.getId(),
                                                u.getName(),
                                                u.getEmail(),
                                                u.getMobile(),
                                                u.getRoles()
                                                                .stream()
                                                                .map(Role::getName)
                                                                .collect(Collectors.toSet())))

                                .toList();
        }

        public List<String> getAllRoles() {
                return roleRepository.findAll()
                                .stream()
                                .map(Role::getName)
                                .toList();
        }

        public void updateUserRoles(UpdateRoleRequest req) {

                User user = userRepository.findById(req.userId())
                                .orElseThrow(() -> new UserNotFoundException("User not found"));

                Set<Role> newRoles = req.roles().stream()
                                .map(roleName -> roleRepository.findByName(roleName)
                                                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName)))
                                .collect(Collectors.toSet());

                user.setRoles(newRoles);
                userRepository.save(user);
        }

        public void removeUserRole(UUID userId, String roleName) {
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new UserNotFoundException("User not found"));

                Role roleToRemove = roleRepository.findByName(roleName)
                                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));

                user.getRoles().remove(roleToRemove);
                userRepository.save(user);
        }

}
