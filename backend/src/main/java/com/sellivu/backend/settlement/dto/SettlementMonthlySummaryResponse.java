package com.sellivu.backend.settlement.dto;

import java.math.BigDecimal;

public class SettlementMonthlySummaryResponse {

    private final String yearMonth;
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
    private final int rowCount;

    public SettlementMonthlySummaryResponse(
            String yearMonth,
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
            BigDecimal preferredFeeRefundAmount,
            int rowCount
    ) {
        this.yearMonth = yearMonth;
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
        this.rowCount = rowCount;
    }

    public String getYearMonth() {
        return yearMonth;
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

    public int getRowCount() {
        return rowCount;
    }
}