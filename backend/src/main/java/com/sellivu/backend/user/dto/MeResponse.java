package com.sellivu.backend.user.dto;

public record MeResponse(
        Long userId,
        String email,
        String name,
        String role
) {
}