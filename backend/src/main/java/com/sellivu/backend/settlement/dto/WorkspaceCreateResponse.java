package com.sellivu.backend.settlement.dto;

import com.sellivu.backend.settlement.domain.SettlementWorkspace;
import com.sellivu.backend.settlement.domain.WorkspaceOwnerType;
import com.sellivu.backend.settlement.domain.WorkspaceStatus;

import java.time.LocalDateTime;

public record WorkspaceCreateResponse(
        String workspaceKey,
        String workspaceToken,
        WorkspaceOwnerType ownerType,
        WorkspaceStatus status,
        LocalDateTime expiresAt,
        LocalDateTime createdAt
) {
    public static WorkspaceCreateResponse of(
            SettlementWorkspace workspace,
            String workspaceToken
    ) {
        return new WorkspaceCreateResponse(
                workspace.getWorkspaceKey(),
                workspaceToken,
                workspace.getOwnerType(),
                workspace.getStatus(),
                workspace.getExpiresAt(),
                workspace.getCreatedAt()
        );
    }
}