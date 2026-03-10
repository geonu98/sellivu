package com.sellivu.backend.settlement.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "settlement_order_snapshot",
        indexes = {
                @Index(name = "idx_snapshot_product_order_no", columnList = "productOrderNo"),
                @Index(name = "idx_snapshot_order_no", columnList = "orderNo"),
                @Index(name = "idx_snapshot_match_status", columnList = "matchStatus"),
                @Index(name = "idx_snapshot_paid_at", columnList = "paidAt"),
                @Index(name = "idx_snapshot_settlement_date", columnList = "settlementDate")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SettlementOrderSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 연결 기준 키
     * PRODUCT_ORDER_NO 우선, 없으면 ORDER_NO fallback
     */
    @Column(length = 100)
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

    @Column(nullable = false)
    private int issueCount;

    @Column(nullable = false)
    private LocalDateTime lastAggregatedAt;

    @Builder
    private SettlementOrderSnapshot(
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
            int issueCount,
            LocalDateTime lastAggregatedAt
    ) {
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
        this.lastAggregatedAt = lastAggregatedAt;
    }

    public static SettlementOrderSnapshot create(
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
                .issueCount(issueCount)
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
        this.issueCount = issueCount;
        this.lastAggregatedAt = LocalDateTime.now();
    }
}