package com.sellivu.backend.settlement.dto;

import com.sellivu.backend.settlement.domain.SettlementAnalysisSet;

import java.time.LocalDateTime;

public record WorkspaceSaveResponse(
        Long analysisSetId,
        String name,
        LocalDateTime createdAt
) {
    public static WorkspaceSaveResponse from(SettlementAnalysisSet set) {
        return new WorkspaceSaveResponse(
                set.getId(),
                set.getName(),
                set.getCreatedAt()
        );
    }
}