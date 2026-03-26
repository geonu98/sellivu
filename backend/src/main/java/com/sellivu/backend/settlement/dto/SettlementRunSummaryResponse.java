package com.sellivu.backend.settlement.dto;

import java.math.BigDecimal;

public class SettlementRunSummaryResponse {

    private final long dailyCount;
    private final long monthlyCount;
    private final long orderCount;
    private final long feeCount;
    private final long snapshotCount;
    private final long issueCount;

    private final BigDecimal settlementAmount;
    private final BigDecimal generalSettlementAmount;
    private final BigDecimal fastSettlementAmount;
    private final BigDecimal settlementBaseAmount;
    private final BigDecimal totalFeeAmount;
    private final BigDecimal benefitSettlementAmount;
    private final BigDecimal dailyDeductionRefundAmount;
    private final BigDecimal holdAmount;
    private final BigDecimal bizWalletOffsetAmount;
    private final BigDecimal safeReturnCareCost;
    private final BigDecimal preferredFeeRefundAmount;

    public SettlementRunSummaryResponse(
            long dailyCount,
            long monthlyCount,
            long orderCount,
            long feeCount,
            long snapshotCount,
            long issueCount,
            BigDecimal settlementAmount,
            BigDecimal generalSettlementAmount,
            BigDecimal fastSettlementAmount,
            BigDecimal settlementBaseAmount,
            BigDecimal totalFeeAmount,
            BigDecimal benefitSettlementAmount,
            BigDecimal dailyDeductionRefundAmount,
            BigDecimal holdAmount,
            BigDecimal bizWalletOffsetAmount,
            BigDecimal safeReturnCareCost,
            BigDecimal preferredFeeRefundAmount
    ) {
        this.dailyCount = dailyCount;
        this.monthlyCount = monthlyCount;
        this.orderCount = orderCount;
        this.feeCount = feeCount;
        this.snapshotCount = snapshotCount;
        this.issueCount = issueCount;
        this.settlementAmount = settlementAmount;
        this.generalSettlementAmount = generalSettlementAmount;
        this.fastSettlementAmount = fastSettlementAmount;
        this.settlementBaseAmount = settlementBaseAmount;
        this.totalFeeAmount = totalFeeAmount;
        this.benefitSettlementAmount = benefitSettlementAmount;
        this.dailyDeductionRefundAmount = dailyDeductionRefundAmount;
        this.holdAmount = holdAmount;
        this.bizWalletOffsetAmount = bizWalletOffsetAmount;
        this.safeReturnCareCost = safeReturnCareCost;
        this.preferredFeeRefundAmount = preferredFeeRefundAmount;
    }

    public long getDailyCount() {
        return dailyCount;
    }

    public long getMonthlyCount() {
        return monthlyCount;
    }

    public long getOrderCount() {
        return orderCount;
    }

    public long getFeeCount() {
        return feeCount;
    }

    public long getSnapshotCount() {
        return snapshotCount;
    }

    public long getIssueCount() {
        return issueCount;
    }

    public BigDecimal getSettlementAmount() {
        return settlementAmount;
    }

    public BigDecimal getGeneralSettlementAmount() {
        return generalSettlementAmount;
    }

    public BigDecimal getFastSettlementAmount() {
        return fastSettlementAmount;
    }

    public BigDecimal getSettlementBaseAmount() {
        return settlementBaseAmount;
    }

    public BigDecimal getTotalFeeAmount() {
        return totalFeeAmount;
    }

    public BigDecimal getBenefitSettlementAmount() {
        return benefitSettlementAmount;
    }

    public BigDecimal getDailyDeductionRefundAmount() {
        return dailyDeductionRefundAmount;
    }

    public BigDecimal getHoldAmount() {
        return holdAmount;
    }

    public BigDecimal getBizWalletOffsetAmount() {
        return bizWalletOffsetAmount;
    }

    public BigDecimal getSafeReturnCareCost() {
        return safeReturnCareCost;
    }

    public BigDecimal getPreferredFeeRefundAmount() {
        return preferredFeeRefundAmount;
    }
}