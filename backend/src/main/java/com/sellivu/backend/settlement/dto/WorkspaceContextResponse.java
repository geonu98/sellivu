package com.sellivu.backend.settlement.dto;

import com.sellivu.backend.settlement.domain.AnalysisOptionValue;
import com.sellivu.backend.settlement.domain.SettlementWorkspaceContext;

import java.time.LocalDateTime;

public record WorkspaceContextResponse(
        AnalysisOptionValue storeCouponUsage,
        AnalysisOptionValue naverCouponUsage,
        AnalysisOptionValue pointBenefitUsage,
        AnalysisOptionValue safeReturnCareUsage,
        AnalysisOptionValue bizWalletOffsetUsage,
        AnalysisOptionValue fastSettlementUsage,
        AnalysisOptionValue claimIncluded,
        LocalDateTime updatedAt
) {
    public static WorkspaceContextResponse from(SettlementWorkspaceContext context) {
        return new WorkspaceContextResponse(
                context.getStoreCouponUsage(),
                context.getNaverCouponUsage(),
                context.getPointBenefitUsage(),
                context.getSafeReturnCareUsage(),
                context.getBizWalletOffsetUsage(),
                context.getFastSettlementUsage(),
                context.getClaimIncluded(),
                context.getUpdatedAt()
        );
    }
}