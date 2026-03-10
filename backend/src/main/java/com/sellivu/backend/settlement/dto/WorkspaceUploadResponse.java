package com.sellivu.backend.settlement.dto;

public record WorkspaceUploadResponse(
        SettlementUploadResponse upload,
        WorkspaceFileResponse workspaceFile
) {
    public static WorkspaceUploadResponse of(
            SettlementUploadResponse upload,
            WorkspaceFileResponse workspaceFile
    ) {
        return new WorkspaceUploadResponse(upload, workspaceFile);
    }
}