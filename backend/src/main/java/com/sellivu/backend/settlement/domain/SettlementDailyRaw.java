package com.sellivu.backend.settlement.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "settlement_daily_raw",
        indexes = {
                @Index(name = "idx_daily_raw_run_id", columnList = "run_id"),
                @Index(name = "idx_daily_raw_upload_id", columnList = "upload_id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SettlementDailyRaw {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "run_id", nullable = false)
    private Long runId;

    @Column(name = "upload_id")
    private Long uploadId;

    @Column(name = "row_no")
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

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}