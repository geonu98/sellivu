package com.sellivu.backend.settlement.dto;

import com.sellivu.backend.settlement.domain.SettlementDailyRow;

import java.math.BigDecimal;
import java.time.LocalDate;

public class SettlementDailyRowResponse {

    private final Long id;
    private final Long uploadId;
    private final Integer rowNo;
    private final LocalDate settlementScheduledDate;
    private final LocalDate settlementCompletedDate;
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
    private final String settlementMethod;

    public SettlementDailyRowResponse(
            Long id,
            Long uploadId,
            Integer rowNo,
            LocalDate settlementScheduledDate,
            LocalDate settlementCompletedDate,
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
            String settlementMethod
    ) {
        this.id = id;
        this.uploadId = uploadId;
        this.rowNo = rowNo;
        this.settlementScheduledDate = settlementScheduledDate;
        this.settlementCompletedDate = settlementCompletedDate;
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
        this.settlementMethod = settlementMethod;
    }

    public static SettlementDailyRowResponse from(SettlementDailyRow row) {
        return new SettlementDailyRowResponse(
                row.getId(),
                row.getUploadId(),
                row.getRowNo(),
                row.getSettlementScheduledDate(),
                row.getSettlementCompletedDate(),
                row.getSettlementAmount(),
                row.getGeneralSettlementAmount(),
                row.getFastSettlementAmount(),
                row.getSettlementBaseAmount(),
                row.getTotalFeeAmount(),
                row.getBenefitSettlementAmount(),
                row.getDailyDeductionRefundAmount(),
                row.getHoldAmount(),
                row.getBizWalletOffsetAmount(),
                row.getSafeReturnCareCost(),
                row.getPreferredFeeRefundAmount(),
                row.getSettlementMethod()
        );
    }

    public Long getId() {
        return id;
    }

    public Long getUploadId() {
        return uploadId;
    }

    public Integer getRowNo() {
        return rowNo;
    }

    public LocalDate getSettlementScheduledDate() {
        return settlementScheduledDate;
    }

    public LocalDate getSettlementCompletedDate() {
        return settlementCompletedDate;
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

    public String getSettlementMethod() {
        return settlementMethod;
    }
}