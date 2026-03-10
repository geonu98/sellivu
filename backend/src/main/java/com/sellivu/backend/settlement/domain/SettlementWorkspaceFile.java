package com.sellivu.backend.settlement.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "settlement_workspace_file",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_workspace_file_workspace_upload",
                        columnNames = {"workspace_id", "upload_id"}
                )
        },
        indexes = {
                @Index(name = "idx_workspace_file_workspace_id", columnList = "workspace_id"),
                @Index(name = "idx_workspace_file_upload_id", columnList = "upload_id"),
                @Index(name = "idx_workspace_file_workspace_type_active", columnList = "workspace_id, file_type, active")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SettlementWorkspaceFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "workspace_id", nullable = false)
    private Long workspaceId;

    @Column(name = "upload_id", nullable = false)
    private Long uploadId;

    @Enumerated(EnumType.STRING)
    @Column(name = "file_type", nullable = false, length = 50)
    private SettlementFileType fileType;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    private SettlementWorkspaceFile(
            Long workspaceId,
            Long uploadId,
            SettlementFileType fileType,
            boolean active,
            LocalDateTime createdAt
    ) {
        this.workspaceId = workspaceId;
        this.uploadId = uploadId;
        this.fileType = fileType;
        this.active = active;
        this.createdAt = createdAt;
    }

    public static SettlementWorkspaceFile create(
            Long workspaceId,
            Long uploadId,
            SettlementFileType fileType
    ) {
        return new SettlementWorkspaceFile(
                workspaceId,
                uploadId,
                fileType,
                true,
                LocalDateTime.now()
        );
    }

    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }
}