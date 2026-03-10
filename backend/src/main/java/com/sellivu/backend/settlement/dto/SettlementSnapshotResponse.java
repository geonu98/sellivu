package com.sellivu.backend.settlement.dto;

import com.sellivu.backend.settlement.domain.SettlementOrderSnapshot;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class SettlementSnapshotResponse {

    private final Long id;
    private final String joinKey;
    private final String orderNo;
    private final String productOrderNo;
    private final String matchStatus;
    private final Long orderRowId;
    private final Long feeRowId;
    private final Long orderUploadId;
    private final Long feeUploadId;
    private final String productName;
    private final String optionName;
    private final String sellerProductCode;
    private final String sellerOptionCode;
    private final LocalDate paidAt;
    private final LocalDate settlementDate;
    private final BigDecimal orderSettlementAmount;
    private final BigDecimal orderCommissionAmount;
    private final BigDecimal orderNetAmount;
    private final BigDecimal feeSettlementAmount;
    private final BigDecimal feeCommissionAmount;
    private final BigDecimal feeNetAmount;
    private final BigDecimal resolvedSettlementAmount;
    private final BigDecimal resolvedCommissionAmount;
    private final BigDecimal resolvedNetAmount;
    private final boolean settlementAmountMatched;
    private final boolean commissionAmountMatched;
    private final boolean netAmountMatched;
    private final int issueCount;
    private final String reviewStatus;
    private final LocalDateTime lastAggregatedAt;

    public SettlementSnapshotResponse(
            Long id,
            String joinKey,
            String orderNo,
            String productOrderNo,
            String matchStatus,
            Long orderRowId,
            Long feeRowId,
            Long orderUploadId,
            Long feeUploadId,
            String productName,
            String optionName,
            String sellerProductCode,
            String sellerOptionCode,
            LocalDate paidAt,
            LocalDate settlementDate,
            BigDecimal orderSettlementAmount,
            BigDecimal orderCommissionAmount,
            BigDecimal orderNetAmount,
            BigDecimal feeSettlementAmount,
            BigDecimal feeCommissionAmount,
            BigDecimal feeNetAmount,
            BigDecimal resolvedSettlementAmount,
            BigDecimal resolvedCommissionAmount,
            BigDecimal resolvedNetAmount,
            boolean settlementAmountMatched,
            boolean commissionAmountMatched,
            boolean netAmountMatched,
            int issueCount,
            String reviewStatus,
            LocalDateTime lastAggregatedAt
    ) {
        this.id = id;
        this.joinKey = joinKey;
        this.orderNo = orderNo;
        this.productOrderNo = productOrderNo;
        this.matchStatus = matchStatus;
        this.orderRowId = orderRowId;
        this.feeRowId = feeRowId;
        this.orderUploadId = orderUploadId;
        this.feeUploadId = feeUploadId;
        this.productName = productName;
        this.optionName = optionName;
        this.sellerProductCode = sellerProductCode;
        this.sellerOptionCode = sellerOptionCode;
        this.paidAt = paidAt;
        this.settlementDate = settlementDate;
        this.orderSettlementAmount = orderSettlementAmount;
        this.orderCommissionAmount = orderCommissionAmount;
        this.orderNetAmount = orderNetAmount;
        this.feeSettlementAmount = feeSettlementAmount;
        this.feeCommissionAmount = feeCommissionAmount;
        this.feeNetAmount = feeNetAmount;
        this.resolvedSettlementAmount = resolvedSettlementAmount;
        this.resolvedCommissionAmount = resolvedCommissionAmount;
        this.resolvedNetAmount = resolvedNetAmount;
        this.settlementAmountMatched = settlementAmountMatched;
        this.commissionAmountMatched = commissionAmountMatched;
        this.netAmountMatched = netAmountMatched;
        this.issueCount = issueCount;
        this.reviewStatus = reviewStatus;
        this.lastAggregatedAt = lastAggregatedAt;
    }

    public static SettlementSnapshotResponse from(SettlementOrderSnapshot snapshot) {
        return new SettlementSnapshotResponse(
                snapshot.getId(),
                snapshot.getJoinKey(),
                snapshot.getOrderNo(),
                snapshot.getProductOrderNo(),
                snapshot.getMatchStatus().name(),
                snapshot.getOrderRowId(),
                snapshot.getFeeRowId(),
                snapshot.getOrderUploadId(),
                snapshot.getFeeUploadId(),
                snapshot.getProductName(),
                snapshot.getOptionName(),
                snapshot.getSellerProductCode(),
                snapshot.getSellerOptionCode(),
                snapshot.getPaidAt(),
                snapshot.getSettlementDate(),
                snapshot.getOrderSettlementAmount(),
                snapshot.getOrderCommissionAmount(),
                snapshot.getOrderNetAmount(),
                snapshot.getFeeSettlementAmount(),
                snapshot.getFeeCommissionAmount(),
                snapshot.getFeeNetAmount(),
                snapshot.getResolvedSettlementAmount(),
                snapshot.getResolvedCommissionAmount(),
                snapshot.getResolvedNetAmount(),
                snapshot.isSettlementAmountMatched(),
                snapshot.isCommissionAmountMatched(),
                snapshot.isNetAmountMatched(),
                snapshot.getIssueCount(),
                resolveReviewStatus(snapshot),
                snapshot.getLastAggregatedAt()
        );
    }

    private static String resolveReviewStatus(SettlementOrderSnapshot snapshot) {
        if (snapshot.getIssueCount() == 0) {
            return "OK";
        }
        if ("MATCHED".equals(snapshot.getMatchStatus().name())) {
            return "REVIEW";
        }
        return "ISSUE";
    }

    public Long getId() {
        return id;
    }

    public String getJoinKey() {
        return joinKey;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public String getProductOrderNo() {
        return productOrderNo;
    }

    public String getMatchStatus() {
        return matchStatus;
    }

    public Long getOrderRowId() {
        return orderRowId;
    }

    public Long getFeeRowId() {
        return feeRowId;
    }

    public Long getOrderUploadId() {
        return orderUploadId;
    }

    public Long getFeeUploadId() {
        return feeUploadId;
    }

    public String getProductName() {
        return productName;
    }

    public String getOptionName() {
        return optionName;
    }

    public String getSellerProductCode() {
        return sellerProductCode;
    }

    public String getSellerOptionCode() {
        return sellerOptionCode;
    }

    public LocalDate getPaidAt() {
        return paidAt;
    }

    public LocalDate getSettlementDate() {
        return settlementDate;
    }

    public BigDecimal getOrderSettlementAmount() {
        return orderSettlementAmount;
    }

    public BigDecimal getOrderCommissionAmount() {
        return orderCommissionAmount;
    }

    public BigDecimal getOrderNetAmount() {
        return orderNetAmount;
    }

    public BigDecimal getFeeSettlementAmount() {
        return feeSettlementAmount;
    }

    public BigDecimal getFeeCommissionAmount() {
        return feeCommissionAmount;
    }

    public BigDecimal getFeeNetAmount() {
        return feeNetAmount;
    }

    public BigDecimal getResolvedSettlementAmount() {
        return resolvedSettlementAmount;
    }

    public BigDecimal getResolvedCommissionAmount() {
        return resolvedCommissionAmount;
    }

    public BigDecimal getResolvedNetAmount() {
        return resolvedNetAmount;
    }

    public boolean isSettlementAmountMatched() {
        return settlementAmountMatched;
    }

    public boolean isCommissionAmountMatched() {
        return commissionAmountMatched;
    }

    public boolean isNetAmountMatched() {
        return netAmountMatched;
    }

    public int getIssueCount() {
        return issueCount;
    }

    public String getReviewStatus() {
        return reviewStatus;
    }

    public LocalDateTime getLastAggregatedAt() {
        return lastAggregatedAt;
    }
}