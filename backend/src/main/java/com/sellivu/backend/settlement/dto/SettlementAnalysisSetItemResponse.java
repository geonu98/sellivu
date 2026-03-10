package com.sellivu.backend.settlement.dto;

import com.sellivu.backend.settlement.domain.SettlementAnalysisSetItem;
import com.sellivu.backend.settlement.domain.SettlementFileType;

import java.time.LocalDateTime;

public class SettlementAnalysisSetItemResponse {

    private final Long id;
    private final Long analysisSetId;
    private final Long uploadId;
    private final SettlementFileType fileType;
    private final LocalDateTime createdAt;

    public SettlementAnalysisSetItemResponse(
            Long id,
            Long analysisSetId,
            Long uploadId,
            SettlementFileType fileType,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.analysisSetId = analysisSetId;
        this.uploadId = uploadId;
        this.fileType = fileType;
        this.createdAt = createdAt;
    }

    public static SettlementAnalysisSetItemResponse from(SettlementAnalysisSetItem item) {
        return new SettlementAnalysisSetItemResponse(
                item.getId(),
                item.getAnalysisSetId(),
                item.getUploadId(),
                item.getFileType(),
                item.getCreatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public Long getAnalysisSetId() {
        return analysisSetId;
    }

    public Long getUploadId() {
        return uploadId;
    }

    public SettlementFileType getFileType() {
        return fileType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}