package com.digitalSansad.auth.dto;

import java.util.Set;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String name,
        String email,
        String mobile,
        Set<String> roles) {
}
