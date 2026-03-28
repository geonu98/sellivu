package com.sellivu.backend.settlement.repository;

import com.sellivu.backend.settlement.domain.SettlementDailyRaw;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface SettlementDailyRawRepository extends JpaRepository<SettlementDailyRaw, Long> {

    Page<SettlementDailyRaw> findAllByRunIdOrderByIdAsc(Long runId, Pageable pageable);

    long countByRunId(Long runId);

    List<SettlementDailyRaw> findAllByRunIdOrderBySettlementCompletedDateAscIdAsc(Long runId);

    @Query(value = """
        select count(distinct to_char(settlement_completed_date, 'YYYY-MM'))
        from settlement_daily_raw
        where run_id = :runId
          and settlement_completed_date is not null
    """, nativeQuery = true)
    long countDistinctSettlementCompletedMonthByRunId(@Param("runId") Long runId);

    @Query("""
        select
            coalesce(sum(d.settlementAmount), 0),
            coalesce(sum(d.generalSettlementAmount), 0),
            coalesce(sum(d.fastSettlementAmount), 0),
            coalesce(sum(d.settlementBaseAmount), 0),
            coalesce(sum(d.totalFeeAmount), 0),
            coalesce(sum(d.benefitSettlementAmount), 0),
            coalesce(sum(d.dailyDeductionRefundAmount), 0),
            coalesce(sum(d.holdAmount), 0),
            coalesce(sum(d.bizWalletOffsetAmount), 0),
            coalesce(sum(d.safeReturnCareCost), 0),
            coalesce(sum(d.preferredFeeRefundAmount), 0)
        from SettlementDailyRaw d
        where d.runId = :runId
    """)
    DailySummaryAggregateProjection aggregateSummaryByRunId(@Param("runId") Long runId);

    interface DailySummaryAggregateProjection {
        BigDecimal getSettlementAmount();
        BigDecimal getGeneralSettlementAmount();
        BigDecimal getFastSettlementAmount();
        BigDecimal getSettlementBaseAmount();
        BigDecimal getTotalFeeAmount();
        BigDecimal getBenefitSettlementAmount();
        BigDecimal getDailyDeductionRefundAmount();
        BigDecimal getHoldAmount();
        BigDecimal getBizWalletOffsetAmount();
        BigDecimal getSafeReturnCareCost();
        BigDecimal getPreferredFeeRefundAmount();
    }
}