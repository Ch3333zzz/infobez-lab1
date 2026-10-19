package com.example.studentapi.auth;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn) {
}
