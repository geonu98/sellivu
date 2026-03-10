package com.sellivu.backend.settlement.dto;

import com.sellivu.backend.settlement.domain.MatchStatus;
import com.sellivu.backend.settlement.domain.SettlementOrderSnapshot;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SettlementSnapshotDetailResponse {

    private Long snapshotId;
    private String joinKey;
    private String orderNo;
    private String productOrderNo;

    private MatchStatus matchStatus;

    private Long orderRowId;
    private Long feeRowId;

    private Long orderUploadId;
    private Long feeUploadId;

    private String productName;
    private String optionName;
    private String sellerProductCode;
    private String sellerOptionCode;

    private LocalDate paidAt;
    private LocalDate settlementDate;

    private BigDecimal orderSettlementAmount;
    private BigDecimal orderCommissionAmount;
    private BigDecimal orderNetAmount;

    private BigDecimal feeSettlementAmount;
    private BigDecimal feeCommissionAmount;
    private BigDecimal feeNetAmount;

    private BigDecimal resolvedSettlementAmount;
    private BigDecimal resolvedCommissionAmount;
    private BigDecimal resolvedNetAmount;

    private boolean settlementAmountMatched;
    private boolean commissionAmountMatched;
    private boolean netAmountMatched;

    private int issueCount;
    private String reviewStatus;
    private LocalDateTime lastAggregatedAt;

    private List<SettlementIssueResponse> issues;

    public static SettlementSnapshotDetailResponse from(
            SettlementOrderSnapshot snapshot,
            List<SettlementIssueResponse> issues
    ) {
        return SettlementSnapshotDetailResponse.builder()
                .snapshotId(snapshot.getId())
                .joinKey(snapshot.getJoinKey())
                .orderNo(snapshot.getOrderNo())
                .productOrderNo(snapshot.getProductOrderNo())
                .matchStatus(snapshot.getMatchStatus())
                .orderRowId(snapshot.getOrderRowId())
                .feeRowId(snapshot.getFeeRowId())
                .orderUploadId(snapshot.getOrderUploadId())
                .feeUploadId(snapshot.getFeeUploadId())
                .productName(snapshot.getProductName())
                .optionName(snapshot.getOptionName())
                .sellerProductCode(snapshot.getSellerProductCode())
                .sellerOptionCode(snapshot.getSellerOptionCode())
                .paidAt(snapshot.getPaidAt())
                .settlementDate(snapshot.getSettlementDate())
                .orderSettlementAmount(snapshot.getOrderSettlementAmount())
                .orderCommissionAmount(snapshot.getOrderCommissionAmount())
                .orderNetAmount(snapshot.getOrderNetAmount())
                .feeSettlementAmount(snapshot.getFeeSettlementAmount())
                .feeCommissionAmount(snapshot.getFeeCommissionAmount())
                .feeNetAmount(snapshot.getFeeNetAmount())
                .resolvedSettlementAmount(snapshot.getResolvedSettlementAmount())
                .resolvedCommissionAmount(snapshot.getResolvedCommissionAmount())
                .resolvedNetAmount(snapshot.getResolvedNetAmount())
                .settlementAmountMatched(snapshot.isSettlementAmountMatched())
                .commissionAmountMatched(snapshot.isCommissionAmountMatched())
                .netAmountMatched(snapshot.isNetAmountMatched())
                .issueCount(snapshot.getIssueCount())
                .reviewStatus(resolveReviewStatus(snapshot))
                .lastAggregatedAt(snapshot.getLastAggregatedAt())
                .issues(issues)
                .build();
    }

    private static String resolveReviewStatus(SettlementOrderSnapshot snapshot) {
        if (snapshot.getIssueCount() == 0) {
            return "OK";
        }
        if (snapshot.getMatchStatus() == MatchStatus.MATCHED) {
            return "REVIEW";
        }
        return "ISSUE";
    }
}