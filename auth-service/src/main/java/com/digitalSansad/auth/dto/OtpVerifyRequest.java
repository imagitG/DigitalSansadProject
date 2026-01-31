package com.digitalSansad.auth.dto;

public record OtpVerifyRequest(
        String email,
        String otp) {
}
