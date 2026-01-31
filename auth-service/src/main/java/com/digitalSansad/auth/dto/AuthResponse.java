package com.digitalSansad.auth.dto;

public record AuthResponse(
                String message,
                int expiresIn,
                String token) {
}
