package com.digitalSansad.auth.dto;

import java.util.List;
import java.util.UUID;

import com.digitalSansad.auth.entity.Role;

//request is of type string not role
public record UpdateRoleRequest(
        UUID userId,
        List<String> roles) {
}
