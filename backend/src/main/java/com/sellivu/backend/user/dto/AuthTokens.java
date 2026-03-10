package com.sellivu.backend.user.dto;
//서비스 내부용
public record AuthTokens(
        Long userId,
        String email,
        String name,
        String role,
        String accessToken,
        String refreshToken
) {
}