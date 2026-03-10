package com.sellivu.backend.settlement.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Entity
@Table(
        name = "settlement_workspace",
        indexes = {
                @Index(name = "idx_workspace_user_id", columnList = "user_id"),
                @Index(name = "idx_workspace_status_expires_at", columnList = "status, expires_at")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_workspace_workspace_key",
                        columnNames = "workspace_key"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SettlementWorkspace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "workspace_key", nullable = false, length = 36)
    private String workspaceKey;

    @Column(name = "access_token_hash", nullable = false, length = 255)
    private String accessTokenHash;

    @Column(name = "user_id")
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "owner_type", nullable = false, length = 20)
    private WorkspaceOwnerType ownerType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private WorkspaceStatus status;

    @Column(name = "saved_analysis_set_id")
    private Long savedAnalysisSetId;

    @Column(name = "last_accessed_at", nullable = false)
    private LocalDateTime lastAccessedAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    private SettlementWorkspace(
            String workspaceKey,
            String accessTokenHash,
            Long userId,
            WorkspaceOwnerType ownerType,
            WorkspaceStatus status,
            LocalDateTime lastAccessedAt,
            LocalDateTime expiresAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.workspaceKey = workspaceKey;
        this.accessTokenHash = accessTokenHash;
        this.userId = userId;
        this.ownerType = ownerType;
        this.status = status;
        this.lastAccessedAt = lastAccessedAt;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static SettlementWorkspace createGuest(
            String accessTokenHash,
            LocalDateTime expiresAt
    ) {
        LocalDateTime now = LocalDateTime.now();
        return new SettlementWorkspace(
                UUID.randomUUID().toString(),
                accessTokenHash,
                null,
                WorkspaceOwnerType.GUEST,
                WorkspaceStatus.ACTIVE,
                now,
                expiresAt,
                now,
                now
        );
    }

    public static SettlementWorkspace createUser(
            Long userId,
            String accessTokenHash,
            LocalDateTime expiresAt
    ) {
        LocalDateTime now = LocalDateTime.now();
        return new SettlementWorkspace(
                UUID.randomUUID().toString(),
                accessTokenHash,
                userId,
                WorkspaceOwnerType.USER,
                WorkspaceStatus.ACTIVE,
                now,
                expiresAt,
                now,
                now
        );
    }

    public void touch() {
        LocalDateTime now = LocalDateTime.now();
        this.lastAccessedAt = now;
        this.updatedAt = now;
    }

    public void assignUser(Long userId) {
        this.userId = userId;
        this.ownerType = WorkspaceOwnerType.USER;
        this.updatedAt = LocalDateTime.now();
    }

    public void markSaved(Long analysisSetId) {
        this.savedAnalysisSetId = analysisSetId;
        this.status = WorkspaceStatus.SAVED;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateSavedAnalysisSetId(Long analysisSetId) {
        this.savedAnalysisSetId = analysisSetId;
        this.updatedAt = LocalDateTime.now();
    }

    public void clearSavedAnalysisSetId() {
        this.savedAnalysisSetId = null;
        this.updatedAt = LocalDateTime.now();
    }

    public void markExpired() {
        this.status = WorkspaceStatus.EXPIRED;
        this.updatedAt = LocalDateTime.now();
    }

    public void extendExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isExpired() {
        return this.expiresAt.isBefore(LocalDateTime.now());
    }

    public boolean isActive() {
        return this.status == WorkspaceStatus.ACTIVE;
    }
}