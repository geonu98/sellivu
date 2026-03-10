package com.sellivu.backend.user.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_token")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "token", nullable = false, length = 1000)
    private String token;

    @Column(name = "expiry_at", nullable = false)
    private LocalDateTime expiryAt;

    protected RefreshToken() {
    }

    public RefreshToken(Long userId, String token, LocalDateTime expiryAt) {
        this.userId = userId;
        this.token = token;
        this.expiryAt = expiryAt;
    }

    public static RefreshToken create(Long userId, String token, LocalDateTime expiryAt) {
        return new RefreshToken(userId, token, expiryAt);
    }

    public void update(String token, LocalDateTime expiryAt) {
        this.token = token;
        this.expiryAt = expiryAt;
    }

    public boolean isExpired() {
        return expiryAt.isBefore(LocalDateTime.now());
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getToken() {
        return token;
    }

    public LocalDateTime getExpiryAt() {
        return expiryAt;
    }
}