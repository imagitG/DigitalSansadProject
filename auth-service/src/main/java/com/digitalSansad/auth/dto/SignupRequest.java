package com.digitalSansad.auth.dto;

public record SignupRequest(
        String name,
        String designation,
        String email,
        String mobile) {
}
