package com.sellivu.backend.settlement.dto;

import com.sellivu.backend.settlement.domain.SettlementFeeRow;

import java.math.BigDecimal;
import java.time.LocalDate;

public class SettlementFeeRowResponse {

    private final Long id;
    private final Long uploadId;
    private final Integer rowNo;
    private final String orderNo;
    private final String productOrderNo;
    private final String sectionType;
    private final String productName;
    private final String buyerName;
    private final LocalDate settlementScheduledDate;
    private final LocalDate settlementCompletedDate;
    private final LocalDate settlementBaseDate;
    private final LocalDate taxReportBaseDate;
    private final String settlementStatus;
    private final BigDecimal feeBaseAmount;
    private final String feeType;
    private final String paymentMethod;
    private final String salesLinkedFeeDetail;
    private final BigDecimal feeCapAmount;
    private final BigDecimal commissionAmount;

    public SettlementFeeRowResponse(
            Long id,
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
        this.id = id;
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

    public static SettlementFeeRowResponse from(SettlementFeeRow row) {
        return new SettlementFeeRowResponse(
                row.getId(),
                row.getUploadId(),
                row.getRowNo(),
                row.getOrderNo(),
                row.getProductOrderNo(),
                row.getSectionType(),
                row.getProductName(),
                row.getBuyerName(),
                row.getSettlementScheduledDate(),
                row.getSettlementCompletedDate(),
                row.getSettlementBaseDate(),
                row.getTaxReportBaseDate(),
                row.getSettlementStatus(),
                row.getFeeBaseAmount(),
                row.getFeeType(),
                row.getPaymentMethod(),
                row.getSalesLinkedFeeDetail(),
                row.getFeeCapAmount(),
                row.getCommissionAmount()
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