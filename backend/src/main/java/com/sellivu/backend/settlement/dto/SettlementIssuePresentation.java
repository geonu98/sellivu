package com.sellivu.backend.settlement.dto;

public record SettlementIssuePresentation(
        String displayCategory,
        String title,
        String description,
        String impact,
        String actionGuide,
        String statusLabel,
        boolean explainable,
        boolean refundCandidate
) {
}