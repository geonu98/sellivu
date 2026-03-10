package com.sellivu.backend.settlement.dto;

public class SettlementAnalysisRebuildResponse {

    private final String message;
    private final Long analysisSetId;
    private final int rebuiltUploadCount;
    private final int deletedSnapshotCount;

    public SettlementAnalysisRebuildResponse(
            String message,
            Long analysisSetId,
            int rebuiltUploadCount,
            int deletedSnapshotCount
    ) {
        this.message = message;
        this.analysisSetId = analysisSetId;
        this.rebuiltUploadCount = rebuiltUploadCount;
        this.deletedSnapshotCount = deletedSnapshotCount;
    }

    public String getMessage() {
        return message;
    }

    public Long getAnalysisSetId() {
        return analysisSetId;
    }

    public int getRebuiltUploadCount() {
        return rebuiltUploadCount;
    }

    public int getDeletedSnapshotCount() {
        return deletedSnapshotCount;
    }
}