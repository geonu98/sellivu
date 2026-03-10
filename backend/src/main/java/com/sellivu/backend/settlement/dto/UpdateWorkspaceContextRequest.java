package com.sellivu.backend.settlement.dto;

import com.sellivu.backend.settlement.domain.AnalysisOptionValue;

public record UpdateWorkspaceContextRequest(
        AnalysisOptionValue storeCouponUsage,
        AnalysisOptionValue naverCouponUsage,
        AnalysisOptionValue pointBenefitUsage,
        AnalysisOptionValue safeReturnCareUsage,
        AnalysisOptionValue bizWalletOffsetUsage,
        AnalysisOptionValue fastSettlementUsage,
        AnalysisOptionValue claimIncluded
) {
}