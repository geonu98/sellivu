package com.sellivu.backend.settlement.service;

import com.sellivu.backend.settlement.domain.SettlementAnalysisSet;
import com.sellivu.backend.settlement.domain.SettlementAnalysisSetItem;
import com.sellivu.backend.settlement.domain.SettlementDailyRow;
import com.sellivu.backend.settlement.domain.SettlementFileType;
import com.sellivu.backend.settlement.dto.SettlementMonthlySummaryResponse;
import com.sellivu.backend.settlement.repository.SettlementAnalysisSetItemRepository;
import com.sellivu.backend.settlement.repository.SettlementAnalysisSetRepository;
import com.sellivu.backend.settlement.repository.SettlementDailyRowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SettlementAnalysisMonthlyService {

    private final SettlementAnalysisSetRepository analysisSetRepository;
    private final SettlementAnalysisSetItemRepository analysisSetItemRepository;
    private final SettlementDailyRowRepository settlementDailyRowRepository;

    public List<SettlementMonthlySummaryResponse> getMonthlySummaries(Long analysisSetId) {
        SettlementAnalysisSet analysisSet = analysisSetRepository.findById(analysisSetId)
                .orElseThrow(() -> new IllegalArgumentException("해당 분석 세트를 찾을 수 없습니다. id=" + analysisSetId));

        List<Long> dailyUploadIds = analysisSetItemRepository.findAllByAnalysisSetIdOrderByIdAsc(analysisSet.getId()).stream()
                .filter(item -> item.getFileType() == SettlementFileType.DAILY_SETTLEMENT)
                .map(SettlementAnalysisSetItem::getUploadId)
                .distinct()
                .toList();

        if (dailyUploadIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<SettlementDailyRow> rows =
                settlementDailyRowRepository.findAllByUploadIdInOrderBySettlementCompletedDateAscIdAsc(dailyUploadIds);

        Map<YearMonth, MonthlyAccumulator> grouped = new TreeMap<>();

        for (SettlementDailyRow row : rows) {
            LocalDate baseDate = row.getSettlementCompletedDate() != null
                    ? row.getSettlementCompletedDate()
                    : row.getSettlementScheduledDate();

            if (baseDate == null) {
                continue;
            }

            YearMonth yearMonth = YearMonth.from(baseDate);
            MonthlyAccumulator acc = grouped.computeIfAbsent(yearMonth, ignored -> new MonthlyAccumulator());

            acc.settlementAmount = acc.settlementAmount.add(nullSafe(row.getSettlementAmount()));
            acc.generalSettlementAmount = acc.generalSettlementAmount.add(nullSafe(row.getGeneralSettlementAmount()));
            acc.fastSettlementAmount = acc.fastSettlementAmount.add(nullSafe(row.getFastSettlementAmount()));
            acc.settlementBaseAmount = acc.settlementBaseAmount.add(nullSafe(row.getSettlementBaseAmount()));
            acc.totalFeeAmount = acc.totalFeeAmount.add(nullSafe(row.getTotalFeeAmount()));
            acc.benefitSettlementAmount = acc.benefitSettlementAmount.add(nullSafe(row.getBenefitSettlementAmount()));
            acc.dailyDeductionRefundAmount = acc.dailyDeductionRefundAmount.add(nullSafe(row.getDailyDeductionRefundAmount()));
            acc.holdAmount = acc.holdAmount.add(nullSafe(row.getHoldAmount()));
            acc.bizWalletOffsetAmount = acc.bizWalletOffsetAmount.add(nullSafe(row.getBizWalletOffsetAmount()));
            acc.safeReturnCareCost = acc.safeReturnCareCost.add(nullSafe(row.getSafeReturnCareCost()));
            acc.preferredFeeRefundAmount = acc.preferredFeeRefundAmount.add(nullSafe(row.getPreferredFeeRefundAmount()));
            acc.rowCount++;
        }

        List<SettlementMonthlySummaryResponse> result = new ArrayList<>();

        for (Map.Entry<YearMonth, MonthlyAccumulator> entry : grouped.entrySet()) {
            YearMonth yearMonth = entry.getKey();
            MonthlyAccumulator acc = entry.getValue();

            result.add(new SettlementMonthlySummaryResponse(
                    yearMonth.toString(),
                    acc.settlementAmount,
                    acc.generalSettlementAmount,
                    acc.fastSettlementAmount,
                    acc.settlementBaseAmount,
                    acc.totalFeeAmount,
                    acc.benefitSettlementAmount,
                    acc.dailyDeductionRefundAmount,
                    acc.holdAmount,
                    acc.bizWalletOffsetAmount,
                    acc.safeReturnCareCost,
                    acc.preferredFeeRefundAmount,
                    acc.rowCount
            ));
        }

        return result;
    }

    private BigDecimal nullSafe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private static class MonthlyAccumulator {
        private BigDecimal settlementAmount = BigDecimal.ZERO;
        private BigDecimal generalSettlementAmount = BigDecimal.ZERO;
        private BigDecimal fastSettlementAmount = BigDecimal.ZERO;
        private BigDecimal settlementBaseAmount = BigDecimal.ZERO;
        private BigDecimal totalFeeAmount = BigDecimal.ZERO;
        private BigDecimal benefitSettlementAmount = BigDecimal.ZERO;
        private BigDecimal dailyDeductionRefundAmount = BigDecimal.ZERO;
        private BigDecimal holdAmount = BigDecimal.ZERO;
        private BigDecimal bizWalletOffsetAmount = BigDecimal.ZERO;
        private BigDecimal safeReturnCareCost = BigDecimal.ZERO;
        private BigDecimal preferredFeeRefundAmount = BigDecimal.ZERO;
        private int rowCount = 0;
    }
}