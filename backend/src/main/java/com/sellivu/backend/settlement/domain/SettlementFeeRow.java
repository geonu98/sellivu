package com.sellivu.backend.settlement.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(
        name = "settlement_fee_row",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_settlement_fee_row_upload_row_no",
                        columnNames = {"upload_id", "row_no"}
                )
        }
)
public class SettlementFeeRow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "upload_id", nullable = false)
    private Long uploadId;

    @Column(name = "row_no", nullable = false)
    private Integer rowNo;

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

    protected SettlementFeeRow() {
    }

    public SettlementFeeRow(
            Long uploadId,
            Integer rowNo,
            String orderNo,
            String productOrderNo,
            String sectionType,
            String productName,
            String buyerName,
            LocalDate settlementScheduledDate,
            LocalDate settlementCompletedDate,
            LocalDate settlementBaseDate,
            LocalDate taxReportBaseDate,
            String settlementStatus,
            BigDecimal feeBaseAmount,
            String feeType,
            String paymentMethod,
            String salesLinkedFeeDetail,
            BigDecimal feeCapAmount,
            BigDecimal commissionAmount
    ) {
        this.uploadId = uploadId;
        this.rowNo = rowNo;
        this.orderNo = orderNo;
        this.productOrderNo = productOrderNo;
        this.sectionType = sectionType;
        this.productName = productName;
        this.buyerName = buyerName;
        this.settlementScheduledDate = settlementScheduledDate;
        this.settlementCompletedDate = settlementCompletedDate;
        this.settlementBaseDate = settlementBaseDate;
        this.taxReportBaseDate = taxReportBaseDate;
        this.settlementStatus = settlementStatus;
        this.feeBaseAmount = feeBaseAmount;
        this.feeType = feeType;
        this.paymentMethod = paymentMethod;
        this.salesLinkedFeeDetail = salesLinkedFeeDetail;
        this.feeCapAmount = feeCapAmount;
        this.commissionAmount = commissionAmount;
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

    public String getOrderNo() {
        return orderNo;
    }

    public String getProductOrderNo() {
        return productOrderNo;
    }

    public String getSectionType() {
        return sectionType;
    }

    public String getProductName() {
        return productName;
    }

    public String getBuyerName() {
        return buyerName;
    }

    public LocalDate getSettlementScheduledDate() {
        return settlementScheduledDate;
    }

    public LocalDate getSettlementCompletedDate() {
        return settlementCompletedDate;
    }

    public LocalDate getSettlementBaseDate() {
        return settlementBaseDate;
    }

    public LocalDate getTaxReportBaseDate() {
        return taxReportBaseDate;
    }

    public String getSettlementStatus() {
        return settlementStatus;
    }

    public BigDecimal getFeeBaseAmount() {
        return feeBaseAmount;
    }

    public String getFeeType() {
        return feeType;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getSalesLinkedFeeDetail() {
        return salesLinkedFeeDetail;
    }

    public BigDecimal getFeeCapAmount() {
        return feeCapAmount;
    }

    public BigDecimal getCommissionAmount() {
        return commissionAmount;
    }
}