package com.sellivu.backend.user.dto;

public record AuthResponse(
        Long userId,
        String email,
        String name,
        String role,
        String accessToken
) {
}