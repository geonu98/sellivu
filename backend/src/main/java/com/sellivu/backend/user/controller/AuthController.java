package com.sellivu.backend.user.controller;

import com.sellivu.backend.global.config.AuthCookieProperties;
import com.sellivu.backend.global.error.ApiException;
import com.sellivu.backend.global.error.ErrorCode;
import com.sellivu.backend.global.security.CustomUserPrincipal;
import com.sellivu.backend.user.dto.AuthResponse;
import com.sellivu.backend.user.dto.AuthTokens;
import com.sellivu.backend.user.dto.LoginRequest;
import com.sellivu.backend.user.dto.MeResponse;
import com.sellivu.backend.user.dto.SignUpRequest;
import com.sellivu.backend.user.dto.TokenRefreshResponse;
import com.sellivu.backend.user.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AuthController {

    private static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";

    private final UserService userService;

    private final AuthCookieProperties authCookieProperties;

    @PostMapping("/auth/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse signUp(
            @Valid @RequestBody SignUpRequest request,
            HttpServletResponse response
    ) {
        AuthTokens tokens = userService.signUp(request);
        addRefreshTokenCookie(response, tokens.refreshToken());

        return new AuthResponse(
                tokens.userId(),
                tokens.email(),
                tokens.name(),
                tokens.role(),
                tokens.accessToken()
        );
    }

    @PostMapping("/auth/login")
    public AuthResponse login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response
    ) {
        AuthTokens tokens = userService.login(request);
        addRefreshTokenCookie(response, tokens.refreshToken());

        return new AuthResponse(
                tokens.userId(),
                tokens.email(),
                tokens.name(),
                tokens.role(),
                tokens.accessToken()
        );
    }

    @PostMapping("/auth/refresh")
    public TokenRefreshResponse refresh(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String refreshToken = extractRefreshTokenFromCookie(request);
        AuthTokens tokens = userService.refresh(refreshToken);

        addRefreshTokenCookie(response, tokens.refreshToken());

        return new TokenRefreshResponse(tokens.accessToken());
    }

    @PostMapping("/auth/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String refreshToken = extractRefreshTokenFromCookie(request);
        userService.logout(refreshToken);
        deleteRefreshTokenCookie(response);
    }

    @GetMapping("/users/me")
    public MeResponse me(@AuthenticationPrincipal CustomUserPrincipal principal) {
        return userService.getMe(principal);
    }
    private void addRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, refreshToken)
                .httpOnly(true)
                .secure(authCookieProperties.secure())
                .sameSite(authCookieProperties.sameSite())
                .path("/api/auth")
                .maxAge(Duration.ofSeconds(authCookieProperties.maxAgeSeconds()));

        if (authCookieProperties.domain() != null && !authCookieProperties.domain().isBlank()) {
            builder.domain(authCookieProperties.domain());
        }

        response.addHeader(HttpHeaders.SET_COOKIE, builder.build().toString());
    }

    private void deleteRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(authCookieProperties.secure())
                .sameSite(authCookieProperties.sameSite())
                .path("/api/auth")
                .maxAge(0);

        if (authCookieProperties.domain() != null && !authCookieProperties.domain().isBlank()) {
            builder.domain(authCookieProperties.domain());
        }

        response.addHeader(HttpHeaders.SET_COOKIE, builder.build().toString());
    }

    private String extractRefreshTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            throw new ApiException(ErrorCode.AUTH_MISSING_TOKEN, "리프레시 토큰 쿠키가 없습니다.");
        }

        for (Cookie cookie : cookies) {
            if (REFRESH_TOKEN_COOKIE_NAME.equals(cookie.getName())) {
                String value = cookie.getValue();
                if (value == null || value.isBlank()) {
                    throw new ApiException(ErrorCode.AUTH_MISSING_TOKEN, "리프레시 토큰 쿠키가 비어 있습니다.");
                }
                return value;
            }
        }

        throw new ApiException(ErrorCode.AUTH_MISSING_TOKEN, "리프레시 토큰 쿠키가 없습니다.");
    }
}