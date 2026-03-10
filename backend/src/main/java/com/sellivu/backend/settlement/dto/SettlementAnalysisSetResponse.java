package com.sellivu.backend.settlement.dto;

import com.sellivu.backend.settlement.domain.SettlementAnalysisSet;

import java.time.LocalDateTime;

public class SettlementAnalysisSetResponse {

    private final Long id;
    private final String name;
    private final LocalDateTime createdAt;

    public SettlementAnalysisSetResponse(Long id, String name, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
    }

    public static SettlementAnalysisSetResponse from(SettlementAnalysisSet set) {
        return new SettlementAnalysisSetResponse(
                set.getId(),
                set.getName(),
                set.getCreatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}