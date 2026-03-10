package com.sellivu.backend.settlement.dto;

import com.sellivu.backend.settlement.domain.MatchStatus;
import com.sellivu.backend.settlement.domain.SettlementOrderSnapshot;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SettlementSnapshotSummaryResponse {

    private Long snapshotId;
    private String joinKey;
    private String orderNo;
    private String productOrderNo;
    private String productName;
    private String optionName;
    private LocalDate paidAt;
    private LocalDate settlementDate;
    private MatchStatus matchStatus;

    private BigDecimal resolvedSettlementAmount;
    private BigDecimal resolvedCommissionAmount;
    private BigDecimal resolvedNetAmount;

    private boolean settlementAmountMatched;
    private boolean commissionAmountMatched;
    private boolean netAmountMatched;

    private int issueCount;

    public static SettlementSnapshotSummaryResponse from(SettlementOrderSnapshot snapshot) {
        return SettlementSnapshotSummaryResponse.builder()
                .snapshotId(snapshot.getId())
                .joinKey(snapshot.getJoinKey())
                .orderNo(snapshot.getOrderNo())
                .productOrderNo(snapshot.getProductOrderNo())
                .productName(snapshot.getProductName())
                .optionName(snapshot.getOptionName())
                .paidAt(snapshot.getPaidAt())
                .settlementDate(snapshot.getSettlementDate())
                .matchStatus(snapshot.getMatchStatus())
                .resolvedSettlementAmount(snapshot.getResolvedSettlementAmount())
                .resolvedCommissionAmount(snapshot.getResolvedCommissionAmount())
                .resolvedNetAmount(snapshot.getResolvedNetAmount())
                .settlementAmountMatched(snapshot.isSettlementAmountMatched())
                .commissionAmountMatched(snapshot.isCommissionAmountMatched())
                .netAmountMatched(snapshot.isNetAmountMatched())
                .issueCount(snapshot.getIssueCount())
                .build();
    }
}