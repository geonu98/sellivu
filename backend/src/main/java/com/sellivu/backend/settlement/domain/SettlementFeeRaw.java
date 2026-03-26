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
        name = "settlement_fee_raw",
        indexes = {
                @Index(name = "idx_fee_raw_run_id", columnList = "run_id"),
                @Index(name = "idx_fee_raw_run_join_key", columnList = "run_id, join_key")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SettlementFeeRaw {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "run_id", nullable = false)
    private Long runId;

    @Column(name = "upload_id")
    private Long uploadId;

    @Column(name = "row_no", nullable = false)
    private Integer rowNo;

    @Column(name = "join_key", length = 100)
    private String joinKey;

    @Column(name = "order_no", length = 100)
    private String orderNo;

    @Column(name = "product_order_no", length = 100)
    private String productOrderNo;

    @Column(name = "section_type", length = 100)
    private String sectionType;

    @Column(name = "product_name", length = 500)
    private String productName;

    @Column(name = "buyer_name", length = 100)
    private String buyerName;

    @Column(name = "settlement_scheduled_date")
    private LocalDate settlementScheduledDate;

    @Column(name = "settlement_completed_date")
    private LocalDate settlementCompletedDate;

    @Column(name = "settlement_base_date")
    private LocalDate settlementBaseDate;

    @Column(name = "tax_report_base_date")
    private LocalDate taxReportBaseDate;

    @Column(name = "settlement_status", length = 100)
    private String settlementStatus;

    @Column(name = "fee_base_amount", precision = 19, scale = 2)
    private BigDecimal feeBaseAmount;

    @Column(name = "fee_type", length = 100)
    private String feeType;

    @Column(name = "payment_method", length = 100)
    private String paymentMethod;

    @Column(name = "sales_linked_fee_detail", length = 255)
    private String salesLinkedFeeDetail;

    @Column(name = "fee_cap_amount", precision = 19, scale = 2)
    private BigDecimal feeCapAmount;

    @Column(name = "commission_amount", precision = 19, scale = 2)
    private BigDecimal commissionAmount;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public static SettlementFeeRaw createFromRow(
            Long runId,
            SettlementFeeRow row,
            String joinKey
    ) {
        SettlementFeeRaw raw = new SettlementFeeRaw();
        raw.runId = runId;
        raw.uploadId = row.getUploadId();
        raw.rowNo = row.getRowNo();
        raw.joinKey = joinKey;
        raw.orderNo = row.getOrderNo();
        raw.productOrderNo = row.getProductOrderNo();
        raw.sectionType = row.getSectionType();
        raw.productName = row.getProductName();
        raw.buyerName = row.getBuyerName();
        raw.settlementScheduledDate = row.getSettlementScheduledDate();
        raw.settlementCompletedDate = row.getSettlementCompletedDate();
        raw.settlementBaseDate = row.getSettlementBaseDate();
        raw.taxReportBaseDate = row.getTaxReportBaseDate();
        raw.settlementStatus = row.getSettlementStatus();
        raw.feeBaseAmount = row.getFeeBaseAmount();
        raw.feeType = row.getFeeType();
        raw.paymentMethod = row.getPaymentMethod();
        raw.salesLinkedFeeDetail = row.getSalesLinkedFeeDetail();
        raw.feeCapAmount = row.getFeeCapAmount();
        raw.commissionAmount = row.getCommissionAmount();
        raw.createdAt = LocalDateTime.now();
        return raw;
    }

}