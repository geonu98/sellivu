package com.sellivu.backend.settlement.dto;

import com.sellivu.backend.settlement.domain.SettlementOrderRow;

import java.math.BigDecimal;
import java.time.LocalDate;

public class SettlementOrderRowResponse {

    private final Long id;
    private final Long uploadId;
    private final Integer rowNo;
    private final String orderNo;
    private final String productOrderNo;
    private final String sectionType;
    private final String productName;
    private final String buyerName;
    private final LocalDate paymentDate;
    private final LocalDate amountChangedDate;
    private final LocalDate settlementScheduledDate;
    private final LocalDate settlementCompletedDate;
    private final LocalDate settlementBaseDate;
    private final LocalDate taxReportBaseDate;
    private final String settlementStatus;
    private final BigDecimal settlementBaseAmount;
    private final BigDecimal npayFeeAmount;
    private final BigDecimal salesLinkedFeeTotal;
    private final BigDecimal installmentFeeAmount;
    private final BigDecimal benefitAmount;
    private final BigDecimal settlementExpectedAmount;
    private final String contractNo;

    public SettlementOrderRowResponse(
            Long id,
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
        this.id = id;
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

    public static SettlementOrderRowResponse from(SettlementOrderRow row) {
        return new SettlementOrderRowResponse(
                row.getId(),
                row.getUploadId(),
                row.getRowNo(),
                row.getOrderNo(),
                row.getProductOrderNo(),
                row.getSectionType(),
                row.getProductName(),
                row.getBuyerName(),
                row.getPaymentDate(),
                row.getAmountChangedDate(),
                row.getSettlementScheduledDate(),
                row.getSettlementCompletedDate(),
                row.getSettlementBaseDate(),
                row.getTaxReportBaseDate(),
                row.getSettlementStatus(),
                row.getSettlementBaseAmount(),
                row.getNpayFeeAmount(),
                row.getSalesLinkedFeeTotal(),
                row.getInstallmentFeeAmount(),
                row.getBenefitAmount(),
                row.getSettlementExpectedAmount(),
                row.getContractNo()
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