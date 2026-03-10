package com.sellivu.backend.settlement.dto;

import com.sellivu.backend.settlement.domain.SettlementFileType;
import com.sellivu.backend.settlement.domain.SettlementUpload;
import com.sellivu.backend.settlement.domain.SettlementUploadStatus;
import java.time.LocalDateTime;

public record SettlementUploadResponse(
        Long uploadId,
        String originalFileName,
        String fileHash,
        SettlementFileType fileType,
        SettlementUploadStatus status,
        Long analysisJobId,
        LocalDateTime uploadedAt,
        String message
) {

    public static SettlementUploadResponse from(SettlementUpload upload, String message) {
        return new SettlementUploadResponse(
                upload.getId(),
                upload.getOriginalFileName(),
                upload.getFileHash(),
                upload.getFileType(),
                upload.getStatus(),
                upload.getAnalysisJobId(),
                upload.getUploadedAt(),
                message
        );
    }
}