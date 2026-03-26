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
        name = "settlement_order_raw",
        indexes = {
                @Index(name = "idx_order_raw_run_id", columnList = "run_id"),
                @Index(name = "idx_order_raw_run_join_key", columnList = "run_id, join_key")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SettlementOrderRaw {

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

    @Column(name = "payment_date")
    private LocalDate paymentDate;

    @Column(name = "amount_changed_date")
    private LocalDate amountChangedDate;

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

    @Column(name = "settlement_base_amount", precision = 19, scale = 2)
    private BigDecimal settlementBaseAmount;

    @Column(name = "npay_fee_amount", precision = 19, scale = 2)
    private BigDecimal npayFeeAmount;

    @Column(name = "sales_linked_fee_total", precision = 19, scale = 2)
    private BigDecimal salesLinkedFeeTotal;

    @Column(name = "installment_fee_amount", precision = 19, scale = 2)
    private BigDecimal installmentFeeAmount;

    @Column(name = "benefit_amount", precision = 19, scale = 2)
    private BigDecimal benefitAmount;

    @Column(name = "settlement_expected_amount", precision = 19, scale = 2)
    private BigDecimal settlementExpectedAmount;

    @Column(name = "contract_no", length = 100)
    private String contractNo;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;


    public static SettlementOrderRaw createFromRow(
            Long runId,
            SettlementOrderRow row,
            String joinKey
    ) {
        SettlementOrderRaw raw = new SettlementOrderRaw();
        raw.runId = runId;
        raw.uploadId = row.getUploadId();
        raw.rowNo = row.getRowNo();
        raw.joinKey = joinKey;
        raw.orderNo = row.getOrderNo();
        raw.productOrderNo = row.getProductOrderNo();
        raw.sectionType = row.getSectionType();
        raw.productName = row.getProductName();
        raw.buyerName = row.getBuyerName();
        raw.paymentDate = row.getPaymentDate();
        raw.amountChangedDate = row.getAmountChangedDate();
        raw.settlementScheduledDate = row.getSettlementScheduledDate();
        raw.settlementCompletedDate = row.getSettlementCompletedDate();
        raw.settlementBaseDate = row.getSettlementBaseDate();
        raw.taxReportBaseDate = row.getTaxReportBaseDate();
        raw.settlementStatus = row.getSettlementStatus();
        raw.settlementBaseAmount = row.getSettlementBaseAmount();
        raw.npayFeeAmount = row.getNpayFeeAmount();
        raw.salesLinkedFeeTotal = row.getSalesLinkedFeeTotal();
        raw.installmentFeeAmount = row.getInstallmentFeeAmount();
        raw.benefitAmount = row.getBenefitAmount();
        raw.settlementExpectedAmount = row.getSettlementExpectedAmount();
        raw.contractNo = row.getContractNo();
        raw.createdAt = LocalDateTime.now();
        return raw;
    }
}