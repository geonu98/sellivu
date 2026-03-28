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
    private final boolean hasIssue;
    private final int issueCount;
    private final long issueMask;
    private final String primaryIssueCode;
    private final boolean refundCandidate;
    private final boolean needsUserInput;
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
            boolean hasIssue,
            int issueCount,
            long issueMask,
            String primaryIssueCode,
            boolean refundCandidate,
            boolean needsUserInput,
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
        this.hasIssue = hasIssue;
        this.issueCount = issueCount;
        this.issueMask = issueMask;
        this.primaryIssueCode = primaryIssueCode;
        this.refundCandidate = refundCandidate;
        this.needsUserInput = needsUserInput;
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
                snapshot.isHasIssue(),
                snapshot.getIssueCount(),
                snapshot.getIssueMask(),
                snapshot.getPrimaryIssueCode(),
                snapshot.isRefundCandidate(),
                snapshot.isNeedsUserInput(),
                resolveReviewStatus(snapshot),
                snapshot.getLastAggregatedAt()
        );
    }

    private static String resolveReviewStatus(SettlementOrderSnapshot snapshot) {
        if (!snapshot.isHasIssue()) return "OK";
        if (snapshot.isNeedsUserInput()) return "PENDING";
        if (snapshot.isRefundCandidate()) return "REFUND_CANDIDATE";
        if ("MATCHED".equals(snapshot.getMatchStatus().name())) return "REVIEW";
        return "ISSUE";
    }

    public Long getId() { return id; }
    public String getJoinKey() { return joinKey; }
    public String getOrderNo() { return orderNo; }
    public String getProductOrderNo() { return productOrderNo; }
    public String getMatchStatus() { return matchStatus; }
    public Long getOrderRowId() { return orderRowId; }
    public Long getFeeRowId() { return feeRowId; }
    public Long getOrderUploadId() { return orderUploadId; }
    public Long getFeeUploadId() { return feeUploadId; }
    public String getProductName() { return productName; }
    public LocalDate getPaidAt() { return paidAt; }
    public LocalDate getSettlementDate() { return settlementDate; }
    public BigDecimal getOrderSettlementAmount() { return orderSettlementAmount; }
    public BigDecimal getOrderCommissionAmount() { return orderCommissionAmount; }
    public BigDecimal getOrderNetAmount() { return orderNetAmount; }
    public BigDecimal getFeeSettlementAmount() { return feeSettlementAmount; }
    public BigDecimal getFeeCommissionAmount() { return feeCommissionAmount; }
    public BigDecimal getFeeNetAmount() { return feeNetAmount; }
    public BigDecimal getResolvedSettlementAmount() { return resolvedSettlementAmount; }
    public BigDecimal getResolvedCommissionAmount() { return resolvedCommissionAmount; }
    public BigDecimal getResolvedNetAmount() { return resolvedNetAmount; }
    public boolean isSettlementAmountMatched() { return settlementAmountMatched; }
    public boolean isCommissionAmountMatched() { return commissionAmountMatched; }
    public boolean isNetAmountMatched() { return netAmountMatched; }
    public boolean isHasIssue() { return hasIssue; }
    public int getIssueCount() { return issueCount; }
    public long getIssueMask() { return issueMask; }
    public String getPrimaryIssueCode() { return primaryIssueCode; }
    public boolean isRefundCandidate() { return refundCandidate; }
    public boolean isNeedsUserInput() { return needsUserInput; }
    public String getReviewStatus() { return reviewStatus; }
    public LocalDateTime getLastAggregatedAt() { return lastAggregatedAt; }
}