package com.sellivu.backend.settlement.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(
        name = "settlement_daily_row",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_settlement_daily_row_upload_row_no",
                        columnNames = {"upload_id", "row_no"}
                )
        }
)
public class SettlementDailyRow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "upload_id", nullable = false)
    private Long uploadId;

    @Column(name = "row_no", nullable = false)
    private Integer rowNo;

    @Column(name = "settlement_scheduled_date")
    private LocalDate settlementScheduledDate;

    @Column(name = "settlement_completed_date")
    private LocalDate settlementCompletedDate;

    @Column(name = "settlement_amount", precision = 19, scale = 2)
    private BigDecimal settlementAmount;

    @Column(name = "general_settlement_amount", precision = 19, scale = 2)
    private BigDecimal generalSettlementAmount;

    @Column(name = "fast_settlement_amount", precision = 19, scale = 2)
    private BigDecimal fastSettlementAmount;

    @Column(name = "settlement_base_amount", precision = 19, scale = 2)
    private BigDecimal settlementBaseAmount;

    @Column(name = "total_fee_amount", precision = 19, scale = 2)
    private BigDecimal totalFeeAmount;

    @Column(name = "benefit_settlement_amount", precision = 19, scale = 2)
    private BigDecimal benefitSettlementAmount;

    @Column(name = "daily_deduction_refund_amount", precision = 19, scale = 2)
    private BigDecimal dailyDeductionRefundAmount;

    @Column(name = "hold_amount", precision = 19, scale = 2)
    private BigDecimal holdAmount;

    @Column(name = "biz_wallet_offset_amount", precision = 19, scale = 2)
    private BigDecimal bizWalletOffsetAmount;

    @Column(name = "safe_return_care_cost", precision = 19, scale = 2)
    private BigDecimal safeReturnCareCost;

    @Column(name = "preferred_fee_refund_amount", precision = 19, scale = 2)
    private BigDecimal preferredFeeRefundAmount;

    @Column(name = "settlement_method", length = 100)
    private String settlementMethod;

    protected SettlementDailyRow() {
    }

    public SettlementDailyRow(
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