package com.sellivu.backend.user.service;

import com.sellivu.backend.global.error.ApiException;
import com.sellivu.backend.global.error.ErrorCode;
import com.sellivu.backend.global.security.CustomUserPrincipal;
import com.sellivu.backend.global.security.JwtTokenProvider;
import com.sellivu.backend.user.domain.RefreshToken;
import com.sellivu.backend.user.domain.User;
import com.sellivu.backend.user.dto.AuthTokens;
import com.sellivu.backend.user.dto.LoginRequest;
import com.sellivu.backend.user.dto.MeResponse;
import com.sellivu.backend.user.dto.SignUpRequest;
import com.sellivu.backend.user.repository.RefreshTokenRepository;
import com.sellivu.backend.user.repository.UserRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public AuthTokens signUp(SignUpRequest request) {
        validateDuplicateEmail(request.email());

        User user = User.create(
                request.email(),
                passwordEncoder.encode(request.password()),
                request.name()
        );

        User savedUser = userRepository.save(user);

        String accessToken = jwtTokenProvider.createAccessToken(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getName(),
                savedUser.getRole().name()
        );

        String refreshToken = jwtTokenProvider.createRefreshToken(savedUser.getId());

        saveOrUpdateRefreshToken(savedUser.getId(), refreshToken);

        return new AuthTokens(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getName(),
                savedUser.getRole().name(),
                accessToken,
                refreshToken
        );
    }

    @Transactional
    public AuthTokens login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ApiException(ErrorCode.AUTH_INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new ApiException(ErrorCode.AUTH_INVALID_CREDENTIALS);
        }

        String accessToken = jwtTokenProvider.createAccessToken(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole().name()
        );

        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        saveOrUpdateRefreshToken(user.getId(), refreshToken);

        return new AuthTokens(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole().name(),
                accessToken,
                refreshToken
        );
    }

    public MeResponse getMe(CustomUserPrincipal principal) {
        return new MeResponse(
                principal.getUserId(),
                principal.getEmail(),
                principal.getName(),
                principal.getRole()
        );
    }

    @Transactional
    public AuthTokens refresh(String refreshToken) {
        if (!jwtTokenProvider.isValidToken(refreshToken)) {
            throw new ApiException(ErrorCode.AUTH_INVALID_TOKEN);
        }

        if (!jwtTokenProvider.isRefreshToken(refreshToken)) {
            throw new ApiException(ErrorCode.REFRESH_TOKEN_MISMATCH);
        }

        Long userId = jwtTokenProvider.getUserId(refreshToken);

        RefreshToken savedRefreshToken = refreshTokenRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.REFRESH_TOKEN_NOT_FOUND));

        if (!savedRefreshToken.getToken().equals(refreshToken)) {
            throw new ApiException(ErrorCode.REFRESH_TOKEN_MISMATCH);
        }

        if (savedRefreshToken.isExpired()) {
            refreshTokenRepository.delete(savedRefreshToken);
            throw new ApiException(ErrorCode.REFRESH_TOKEN_EXPIRED);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        String newAccessToken = jwtTokenProvider.createAccessToken(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole().name()
        );

        String newRefreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        savedRefreshToken.update(
                newRefreshToken,
                calculateRefreshTokenExpiryAt()
        );

        return new AuthTokens(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole().name(),
                newAccessToken,
                newRefreshToken
        );
    }

    @Transactional
    public void logout(String refreshToken) {
        if (!jwtTokenProvider.isValidToken(refreshToken)) {
            throw new ApiException(ErrorCode.AUTH_INVALID_TOKEN);
        }

        if (!jwtTokenProvider.isRefreshToken(refreshToken)) {
            throw new ApiException(ErrorCode.REFRESH_TOKEN_MISMATCH);
        }

        Long userId = jwtTokenProvider.getUserId(refreshToken);

        RefreshToken savedRefreshToken = refreshTokenRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.REFRESH_TOKEN_NOT_FOUND));

        if (!savedRefreshToken.getToken().equals(refreshToken)) {
            throw new ApiException(ErrorCode.REFRESH_TOKEN_MISMATCH);
        }

        refreshTokenRepository.delete(savedRefreshToken);
    }

    private void validateDuplicateEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new ApiException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
    }

    private void saveOrUpdateRefreshToken(Long userId, String refreshToken) {
        LocalDateTime expiryAt = calculateRefreshTokenExpiryAt();

        refreshTokenRepository.findByUserId(userId)
                .ifPresentOrElse(
                        savedToken -> savedToken.update(refreshToken, expiryAt),
                        () -> refreshTokenRepository.save(
                                RefreshToken.create(userId, refreshToken, expiryAt)
                        )
                );
    }

    private LocalDateTime calculateRefreshTokenExpiryAt() {
        return LocalDateTime.now()
                .plus(Duration.ofMillis(jwtTokenProvider.getRefreshTokenExpirationMs()));
    }
}