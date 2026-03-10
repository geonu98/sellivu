package com.sellivu.backend.settlement.dto;

import com.sellivu.backend.settlement.domain.SettlementFileType;
import com.sellivu.backend.settlement.domain.SettlementUpload;
import com.sellivu.backend.settlement.domain.SettlementWorkspaceFile;

import java.time.LocalDateTime;

public record WorkspaceFileResponse(
        Long workspaceFileId,
        Long uploadId,
        String originalFileName,
        SettlementFileType fileType,
        boolean active,
        LocalDateTime createdAt
) {
    public static WorkspaceFileResponse of(
            SettlementWorkspaceFile workspaceFile,
            SettlementUpload upload
    ) {
        return new WorkspaceFileResponse(
                workspaceFile.getId(),
                workspaceFile.getUploadId(),
                upload.getOriginalFileName(),
                workspaceFile.getFileType(),
                workspaceFile.isActive(),
                workspaceFile.getCreatedAt()
        );
    }
}