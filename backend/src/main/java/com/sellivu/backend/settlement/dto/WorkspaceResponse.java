package com.sellivu.backend.settlement.dto;

import com.sellivu.backend.settlement.domain.WorkspaceOwnerType;
import com.sellivu.backend.settlement.domain.WorkspaceStatus;

import java.time.LocalDateTime;
import java.util.List;

public record WorkspaceResponse(
        String workspaceKey,
        WorkspaceOwnerType ownerType,
        WorkspaceStatus status,
        Long savedAnalysisSetId,
        LocalDateTime expiresAt,
        List<WorkspaceFileResponse> files,
        WorkspaceContextResponse context,
        AnalysisCapabilityResponse capability
) {
}