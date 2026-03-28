package com.sellivu.backend.settlement.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "settlement_order_snapshot",
        indexes = {
                @Index(name = "idx_snapshot_run_id", columnList = "run_id"),
                @Index(name = "idx_snapshot_run_id_join_key", columnList = "run_id, join_key"),
                @Index(name = "idx_snapshot_join_key", columnList = "join_key")
        }
)
public class SettlementOrderSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "run_id")
    private Long runId;

    @Column(nullable = false, length = 100)
    private String joinKey;

    @Column(length = 100)
    private String orderNo;

    @Column(length = 100)
    private String productOrderNo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MatchStatus matchStatus;

    /**
     * 어떤 원본 row와 연결되었는지 추적
     */
    private Long orderRowId;
    private Long feeRowId;

    /**
     * 업로드 추적
     */
    private Long orderUploadId;
    private Long feeUploadId;

    /**
     * 공통 식별/조회용
     */
    @Column(length = 200)
    private String productName;

    @Column(length = 100)
    private String optionName;

    @Column(length = 100)
    private String sellerProductCode;

    @Column(length = 100)
    private String sellerOptionCode;

    private LocalDate paidAt;
    private LocalDate settlementDate;

    /**
     * 주문 쪽 금액
     */
    @Column(precision = 19, scale = 2)
    private BigDecimal orderSettlementAmount;

    @Column(precision = 19, scale = 2)
    private BigDecimal orderCommissionAmount;

    @Column(precision = 19, scale = 2)
    private BigDecimal orderNetAmount;

    /**
     * 수수료 파일 쪽 금액
     */
    @Column(precision = 19, scale = 2)
    private BigDecimal feeSettlementAmount;

    @Column(precision = 19, scale = 2)
    private BigDecimal feeCommissionAmount;

    @Column(precision = 19, scale = 2)
    private BigDecimal feeNetAmount;

    /**
     * 최종 비교 결과
     */
    @Column(precision = 19, scale = 2)
    private BigDecimal resolvedSettlementAmount;

    @Column(precision = 19, scale = 2)
    private BigDecimal resolvedCommissionAmount;

    @Column(precision = 19, scale = 2)
    private BigDecimal resolvedNetAmount;

    @Column(nullable = false)
    private boolean settlementAmountMatched;

    @Column(nullable = false)
    private boolean commissionAmountMatched;

    @Column(nullable = false)
    private boolean netAmountMatched;

    /**
     * 하이브리드 최종형 이슈 요약 컬럼
     */
    @Column(name = "has_issue", nullable = false)
    private boolean hasIssue;

    @Column(name = "issue_count", nullable = false)
    private int issueCount;

    @Column(name = "issue_mask", nullable = false)
    private long issueMask;

    @Column(name = "primary_issue_code", length = 50)
    private String primaryIssueCode;

    @Column(name = "issue_payload", columnDefinition = "jsonb")
    private String issuePayload;

    @Column(name = "refund_candidate", nullable = false)
    private boolean refundCandidate;

    @Column(name = "needs_user_input", nullable = false)
    private boolean needsUserInput;

    @Column(nullable = false)
    private LocalDateTime lastAggregatedAt;

    @Builder
    private SettlementOrderSnapshot(
            Long runId,
            String joinKey,
            String orderNo,
            String productOrderNo,
            MatchStatus matchStatus,
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
            boolean hasIssue,
            int issueCount,
            long issueMask,
            String primaryIssueCode,
            String issuePayload,
            boolean refundCandidate,
            boolean needsUserInput,
            LocalDateTime lastAggregatedAt
    ) {
        this.runId = runId;
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
        this.hasIssue = hasIssue;
        this.issueCount = issueCount;
        this.issueMask = issueMask;
        this.primaryIssueCode = primaryIssueCode;
        this.issuePayload = issuePayload;
        this.refundCandidate = refundCandidate;
        this.needsUserInput = needsUserInput;
        this.lastAggregatedAt = lastAggregatedAt;
    }

    public static SettlementOrderSnapshot create(
            Long runId,
            String joinKey,
            String orderNo,
            String productOrderNo,
            MatchStatus matchStatus,
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
            int issueCount
    ) {
        return SettlementOrderSnapshot.builder()
                .runId(runId)
                .joinKey(joinKey)
                .orderNo(orderNo)
                .productOrderNo(productOrderNo)
                .matchStatus(matchStatus)
                .orderRowId(orderRowId)
                .feeRowId(feeRowId)
                .orderUploadId(orderUploadId)
                .feeUploadId(feeUploadId)
                .productName(productName)
                .optionName(optionName)
                .sellerProductCode(sellerProductCode)
                .sellerOptionCode(sellerOptionCode)
                .paidAt(paidAt)
                .settlementDate(settlementDate)
                .orderSettlementAmount(orderSettlementAmount)
                .orderCommissionAmount(orderCommissionAmount)
                .orderNetAmount(orderNetAmount)
                .feeSettlementAmount(feeSettlementAmount)
                .feeCommissionAmount(feeCommissionAmount)
                .feeNetAmount(feeNetAmount)
                .resolvedSettlementAmount(resolvedSettlementAmount)
                .resolvedCommissionAmount(resolvedCommissionAmount)
                .resolvedNetAmount(resolvedNetAmount)
                .settlementAmountMatched(settlementAmountMatched)
                .commissionAmountMatched(commissionAmountMatched)
                .netAmountMatched(netAmountMatched)
                .hasIssue(issueCount > 0)
                .issueCount(issueCount)
                .issueMask(0L)
                .primaryIssueCode(null)
                .issuePayload(null)
                .refundCandidate(false)
                .needsUserInput(false)
                .lastAggregatedAt(LocalDateTime.now())
                .build();
    }

    public void update(
            MatchStatus matchStatus,
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
            int issueCount
    ) {
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
        this.hasIssue = issueCount > 0;
        this.issueCount = issueCount;
        this.lastAggregatedAt = LocalDateTime.now();
    }

    public void updateIssueCount(int issueCount) {
        this.hasIssue = issueCount > 0;
        this.issueCount = issueCount;
        this.lastAggregatedAt = LocalDateTime.now();
    }

    public void updateIssueSummary(
            long issueMask,
            int issueCount,
            String primaryIssueCode,
            String issuePayload,
            boolean refundCandidate,
            boolean needsUserInput
    ) {
        this.hasIssue = issueCount > 0;
        this.issueMask = issueMask;
        this.issueCount = issueCount;
        this.primaryIssueCode = primaryIssueCode;
        this.issuePayload = issuePayload;
        this.refundCandidate = refundCandidate;
        this.needsUserInput = needsUserInput;
        this.lastAggregatedAt = LocalDateTime.now();
    }

    public void clearIssueSummary() {
        this.hasIssue = false;
        this.issueCount = 0;
        this.issueMask = 0L;
        this.primaryIssueCode = null;
        this.issuePayload = null;
        this.refundCandidate = false;
        this.needsUserInput = false;
        this.lastAggregatedAt = LocalDateTime.now();
    }
}