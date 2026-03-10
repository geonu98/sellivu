package com.sellivu.backend.settlement.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(
        name = "settlement_order_row",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_settlement_order_row_upload_row_no",
                        columnNames = {"upload_id", "row_no"}
                )
        }
)
public class SettlementOrderRow {

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

    protected SettlementOrderRow() {
    }

    public SettlementOrderRow(
            Long uploadId,
            Integer rowNo,
            String orderNo,
            String productOrderNo,
            String sectionType,
            String productName,
            String buyerName,
            LocalDate paymentDate,
            LocalDate amountChangedDate,
            LocalDate settlementScheduledDate,
            LocalDate settlementCompletedDate,
            LocalDate settlementBaseDate,
            LocalDate taxReportBaseDate,
            String settlementStatus,
            BigDecimal settlementBaseAmount,
            BigDecimal npayFeeAmount,
            BigDecimal salesLinkedFeeTotal,
            BigDecimal installmentFeeAmount,
            BigDecimal benefitAmount,
            BigDecimal settlementExpectedAmount,
            String contractNo
    ) {
        this.uploadId = uploadId;
        this.rowNo = rowNo;
        this.orderNo = orderNo;
        this.productOrderNo = productOrderNo;
        this.sectionType = sectionType;
        this.productName = productName;
        this.buyerName = buyerName;
        this.paymentDate = paymentDate;
        this.amountChangedDate = amountChangedDate;
        this.settlementScheduledDate = settlementScheduledDate;
        this.settlementCompletedDate = settlementCompletedDate;
        this.settlementBaseDate = settlementBaseDate;
        this.taxReportBaseDate = taxReportBaseDate;
        this.settlementStatus = settlementStatus;
        this.settlementBaseAmount = settlementBaseAmount;
        this.npayFeeAmount = npayFeeAmount;
        this.salesLinkedFeeTotal = salesLinkedFeeTotal;
        this.installmentFeeAmount = installmentFeeAmount;
        this.benefitAmount = benefitAmount;
        this.settlementExpectedAmount = settlementExpectedAmount;
        this.contractNo = contractNo;
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

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public LocalDate getAmountChangedDate() {
        return amountChangedDate;
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

    public BigDecimal getSettlementBaseAmount() {
        return settlementBaseAmount;
    }

    public BigDecimal getNpayFeeAmount() {
        return npayFeeAmount;
    }

    public BigDecimal getSalesLinkedFeeTotal() {
        return salesLinkedFeeTotal;
    }

    public BigDecimal getInstallmentFeeAmount() {
        return installmentFeeAmount;
    }

    public BigDecimal getBenefitAmount() {
        return benefitAmount;
    }

    public BigDecimal getSettlementExpectedAmount() {
        return settlementExpectedAmount;
    }

    public String getContractNo() {
        return contractNo;
    }
}