package com.sellivu.backend.settlement.dto;

public class UpdateAnalysisContextRequest {

    private String storeCouponUsage;
    private String naverCouponUsage;
    private String pointBenefitUsage;
    private String safeReturnCareUsage;
    private String bizWalletOffsetUsage;
    private String fastSettlementUsage;
    private String claimIncluded;

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
}