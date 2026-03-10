package com.sellivu.backend.settlement.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "settlement_analysis_context",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_analysis_context_analysis_set",
                        columnNames = "analysis_set_id"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SettlementAnalysisContext {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "analysis_set_id", nullable = false)
    private Long analysisSetId;

    @Enumerated(EnumType.STRING)
    @Column(name = "store_coupon_usage", nullable = false, length = 20)
    private AnalysisOptionValue storeCouponUsage;

    @Enumerated(EnumType.STRING)
    @Column(name = "naver_coupon_usage", nullable = false, length = 20)
    private AnalysisOptionValue naverCouponUsage;

    @Enumerated(EnumType.STRING)
    @Column(name = "point_benefit_usage", nullable = false, length = 20)
    private AnalysisOptionValue pointBenefitUsage;

    @Enumerated(EnumType.STRING)
    @Column(name = "safe_return_care_usage", nullable = false, length = 20)
    private AnalysisOptionValue safeReturnCareUsage;

    @Enumerated(EnumType.STRING)
    @Column(name = "biz_wallet_offset_usage", nullable = false, length = 20)
    private AnalysisOptionValue bizWalletOffsetUsage;

    @Enumerated(EnumType.STRING)
    @Column(name = "fast_settlement_usage", nullable = false, length = 20)
    private AnalysisOptionValue fastSettlementUsage;

    @Enumerated(EnumType.STRING)
    @Column(name = "claim_included", nullable = false, length = 20)
    private AnalysisOptionValue claimIncluded;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    private SettlementAnalysisContext(
            Long analysisSetId,
            AnalysisOptionValue storeCouponUsage,
            AnalysisOptionValue naverCouponUsage,
            AnalysisOptionValue pointBenefitUsage,
            AnalysisOptionValue safeReturnCareUsage,
            AnalysisOptionValue bizWalletOffsetUsage,
            AnalysisOptionValue fastSettlementUsage,
            AnalysisOptionValue claimIncluded,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.analysisSetId = analysisSetId;
        this.storeCouponUsage = storeCouponUsage;
        this.naverCouponUsage = naverCouponUsage;
        this.pointBenefitUsage = pointBenefitUsage;
        this.safeReturnCareUsage = safeReturnCareUsage;
        this.bizWalletOffsetUsage = bizWalletOffsetUsage;
        this.fastSettlementUsage = fastSettlementUsage;
        this.claimIncluded = claimIncluded;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static SettlementAnalysisContext createDefault(Long analysisSetId) {
        LocalDateTime now = LocalDateTime.now();
        return new SettlementAnalysisContext(
                analysisSetId,
                AnalysisOptionValue.UNKNOWN,
                AnalysisOptionValue.UNKNOWN,
                AnalysisOptionValue.UNKNOWN,
                AnalysisOptionValue.UNKNOWN,
                AnalysisOptionValue.UNKNOWN,
                AnalysisOptionValue.UNKNOWN,
                AnalysisOptionValue.UNKNOWN,
                now,
                now
        );
    }

    public void update(
            AnalysisOptionValue storeCouponUsage,
            AnalysisOptionValue naverCouponUsage,
            AnalysisOptionValue pointBenefitUsage,
            AnalysisOptionValue safeReturnCareUsage,
            AnalysisOptionValue bizWalletOffsetUsage,
            AnalysisOptionValue fastSettlementUsage,
            AnalysisOptionValue claimIncluded
    ) {
        this.storeCouponUsage = storeCouponUsage;
        this.naverCouponUsage = naverCouponUsage;
        this.pointBenefitUsage = pointBenefitUsage;
        this.safeReturnCareUsage = safeReturnCareUsage;
        this.bizWalletOffsetUsage = bizWalletOffsetUsage;
        this.fastSettlementUsage = fastSettlementUsage;
        this.claimIncluded = claimIncluded;
        this.updatedAt = LocalDateTime.now();
    }
}