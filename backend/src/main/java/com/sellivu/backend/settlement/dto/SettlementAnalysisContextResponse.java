package com.sellivu.backend.settlement.dto;

import com.sellivu.backend.settlement.domain.SettlementAnalysisContext;

import java.time.LocalDateTime;

public class SettlementAnalysisContextResponse {

    private final Long analysisSetId;
    private final String storeCouponUsage;
    private final String naverCouponUsage;
    private final String pointBenefitUsage;
    private final String safeReturnCareUsage;
    private final String bizWalletOffsetUsage;
    private final String fastSettlementUsage;
    private final String claimIncluded;
    private final LocalDateTime updatedAt;

    public SettlementAnalysisContextResponse(
            Long analysisSetId,
            String storeCouponUsage,
            String naverCouponUsage,
            String pointBenefitUsage,
            String safeReturnCareUsage,
            String bizWalletOffsetUsage,
            String fastSettlementUsage,
            String claimIncluded,
            LocalDateTime updatedAt
    ) {
        this.analysisSetId = analysisSetId;
        this.storeCouponUsage = storeCouponUsage;
        this.naverCouponUsage = naverCouponUsage;
        this.pointBenefitUsage = pointBenefitUsage;
        this.safeReturnCareUsage = safeReturnCareUsage;
        this.bizWalletOffsetUsage = bizWalletOffsetUsage;
        this.fastSettlementUsage = fastSettlementUsage;
        this.claimIncluded = claimIncluded;
        this.updatedAt = updatedAt;
    }

    public static SettlementAnalysisContextResponse from(SettlementAnalysisContext context) {
        return new SettlementAnalysisContextResponse(
                context.getAnalysisSetId(),
                context.getStoreCouponUsage().name(),
                context.getNaverCouponUsage().name(),
                context.getPointBenefitUsage().name(),
                context.getSafeReturnCareUsage().name(),
                context.getBizWalletOffsetUsage().name(),
                context.getFastSettlementUsage().name(),
                context.getClaimIncluded().name(),
                context.getUpdatedAt()
        );
    }

    public Long getAnalysisSetId() {
        return analysisSetId;
    }

    public String getStoreCouponUsage() {
        return storeCouponUsage;
    }

    public String getNaverCouponUsage() {
        return naverCouponUsage;
    }

    public String getPointBenefitUsage() {
        return pointBenefitUsage;
    }

    public String getSafeReturnCareUsage() {
        return safeReturnCareUsage;
    }

    public String getBizWalletOffsetUsage() {
        return bizWalletOffsetUsage;
    }

    public String getFastSettlementUsage() {
        return fastSettlementUsage;
    }

    public String getClaimIncluded() {
        return claimIncluded;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}