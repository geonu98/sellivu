package com.sellivu.backend.settlement.service;

import com.sellivu.backend.global.error.ApiException;
import com.sellivu.backend.global.error.ErrorCode;
import com.sellivu.backend.settlement.domain.SettlementDailyRaw;
import com.sellivu.backend.settlement.domain.SettlementIssue;
import com.sellivu.backend.settlement.domain.SettlementOrderSnapshot;
import com.sellivu.backend.settlement.domain.SettlementWorkspace;
import com.sellivu.backend.settlement.dto.SettlementDailyRowResponse;
import com.sellivu.backend.settlement.dto.SettlementFeeRowResponse;
import com.sellivu.backend.settlement.dto.SettlementMonthlySummaryResponse;
import com.sellivu.backend.settlement.dto.SettlementOrderRowResponse;
import com.sellivu.backend.settlement.dto.SettlementRunSummaryResponse;
import com.sellivu.backend.settlement.repository.SettlementDailyRawRepository;
import com.sellivu.backend.settlement.repository.SettlementFeeRawRepository;
import com.sellivu.backend.settlement.repository.SettlementIssueRepository;
import com.sellivu.backend.settlement.repository.SettlementOrderRawRepository;
import com.sellivu.backend.settlement.repository.SettlementOrderSnapshotRepository;
import com.sellivu.backend.settlement.repository.SettlementWorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SettlementRunQueryService {

    private final SettlementWorkspaceRepository settlementWorkspaceRepository;
    private final SettlementOrderSnapshotRepository settlementOrderSnapshotRepository;
    private final SettlementIssueRepository settlementIssueRepository;
    private final SettlementWorkspaceService settlementWorkspaceService;
    private final SettlementOrderRawRepository settlementOrderRawRepository;
    private final SettlementFeeRawRepository settlementFeeRawRepository;
    private final SettlementDailyRawRepository settlementDailyRawRepository;

    public Long getActiveRunId(Long workspaceId) {
        SettlementWorkspace workspace = settlementWorkspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ApiException(ErrorCode.WORKSPACE_NOT_FOUND));

        return workspace.getActiveRunId();
    }

    private Long getRequiredActiveRunId(Long workspaceId) {
        Long activeRunId = getActiveRunId(workspaceId);
        if (activeRunId == null) {
            throw new ApiException(ErrorCode.WORKSPACE_NOT_FOUND, "현재 활성 분석 결과가 없습니다.");
        }
        return activeRunId;
    }

    private SettlementWorkspace getRequiredWorkspace(String workspaceKey, String workspaceToken) {
        SettlementWorkspace workspace =
                settlementWorkspaceService.validateAccessibleWorkspace(workspaceKey, workspaceToken);

        if (workspace.getActiveRunId() == null) {
            throw new ApiException(ErrorCode.WORKSPACE_NOT_FOUND, "현재 활성 분석 결과가 없습니다.");
        }

        return workspace;
    }

    public Page<SettlementOrderSnapshot> getActiveRunSnapshots(Long workspaceId, int page, int size) {
        Long runId = getRequiredActiveRunId(workspaceId);
        return settlementOrderSnapshotRepository.findAllByRunIdOrderByIdDesc(
                runId,
                PageRequest.of(page, size)
        );
    }

    public Page<SettlementIssue> getActiveRunIssues(Long workspaceId, int page, int size) {
        Long runId = getRequiredActiveRunId(workspaceId);
        return settlementIssueRepository.findAllByRunIdOrderByIdDesc(
                runId,
                PageRequest.of(page, size)
        );
    }

    public Page<SettlementOrderRowResponse> getActiveRunOrders(
            String workspaceKey,
            String workspaceToken,
            int page,
            int size
    ) {
        SettlementWorkspace workspace = getRequiredWorkspace(workspaceKey, workspaceToken);

        return settlementOrderRawRepository.findAllByRunIdOrderByIdAsc(
                        workspace.getActiveRunId(),
                        PageRequest.of(page, size)
                )
                .map(SettlementOrderRowResponse::from);
    }

    public Page<SettlementFeeRowResponse> getActiveRunFees(
            String workspaceKey,
            String workspaceToken,
            int page,
            int size
    ) {
        SettlementWorkspace workspace = getRequiredWorkspace(workspaceKey, workspaceToken);

        return settlementFeeRawRepository.findAllByRunIdOrderByIdAsc(
                        workspace.getActiveRunId(),
                        PageRequest.of(page, size)
                )
                .map(SettlementFeeRowResponse::from);
    }

    public Page<SettlementDailyRowResponse> getActiveRunDaily(
            String workspaceKey,
            String workspaceToken,
            int page,
            int size
    ) {
        SettlementWorkspace workspace = getRequiredWorkspace(workspaceKey, workspaceToken);

        return settlementDailyRawRepository.findAllByRunIdOrderByIdAsc(
                        workspace.getActiveRunId(),
                        PageRequest.of(page, size)
                )
                .map(SettlementDailyRowResponse::from);
    }

    public List<SettlementMonthlySummaryResponse> getActiveRunMonthly(
            String workspaceKey,
            String workspaceToken
    ) {
        SettlementWorkspace workspace = getRequiredWorkspace(workspaceKey, workspaceToken);

        List<SettlementDailyRaw> rows =
                settlementDailyRawRepository.findAllByRunIdOrderBySettlementCompletedDateAscIdAsc(
                        workspace.getActiveRunId()
                );

        Map<String, MonthlyAccumulator> grouped = new LinkedHashMap<>();

        for (SettlementDailyRaw row : rows) {
            if (row.getSettlementCompletedDate() == null) {
                continue;
            }

            String yearMonth = YearMonth.from(row.getSettlementCompletedDate()).toString();
            MonthlyAccumulator acc = grouped.computeIfAbsent(yearMonth, key -> new MonthlyAccumulator());

            acc.settlementAmount = acc.settlementAmount.add(nvl(row.getSettlementAmount()));
            acc.generalSettlementAmount = acc.generalSettlementAmount.add(nvl(row.getGeneralSettlementAmount()));
            acc.fastSettlementAmount = acc.fastSettlementAmount.add(nvl(row.getFastSettlementAmount()));
            acc.settlementBaseAmount = acc.settlementBaseAmount.add(nvl(row.getSettlementBaseAmount()));
            acc.totalFeeAmount = acc.totalFeeAmount.add(nvl(row.getTotalFeeAmount()));
            acc.benefitSettlementAmount = acc.benefitSettlementAmount.add(nvl(row.getBenefitSettlementAmount()));
            acc.dailyDeductionRefundAmount = acc.dailyDeductionRefundAmount.add(nvl(row.getDailyDeductionRefundAmount()));
            acc.holdAmount = acc.holdAmount.add(nvl(row.getHoldAmount()));
            acc.bizWalletOffsetAmount = acc.bizWalletOffsetAmount.add(nvl(row.getBizWalletOffsetAmount()));
            acc.safeReturnCareCost = acc.safeReturnCareCost.add(nvl(row.getSafeReturnCareCost()));
            acc.preferredFeeRefundAmount = acc.preferredFeeRefundAmount.add(nvl(row.getPreferredFeeRefundAmount()));
            acc.rowCount++;
        }

        List<SettlementMonthlySummaryResponse> result = new ArrayList<>();
        for (Map.Entry<String, MonthlyAccumulator> entry : grouped.entrySet()) {
            MonthlyAccumulator acc = entry.getValue();

            result.add(new SettlementMonthlySummaryResponse(
                    entry.getKey(),
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

    public SettlementRunSummaryResponse getActiveRunSummary(
            String workspaceKey,
            String workspaceToken
    ) {
        SettlementWorkspace workspace = getRequiredWorkspace(workspaceKey, workspaceToken);
        Long runId = workspace.getActiveRunId();

        long dailyCount = settlementDailyRawRepository.countByRunId(runId);
        long monthlyCount = getActiveRunMonthly(workspaceKey, workspaceToken).size();
        long orderCount = settlementOrderRawRepository.countByRunId(runId);
        long feeCount = settlementFeeRawRepository.countByRunId(runId);
        long snapshotCount = settlementOrderSnapshotRepository.countByRunId(runId);
        long issueCount = settlementIssueRepository.countByRunId(runId);

        List<SettlementDailyRaw> dailyRows =
                settlementDailyRawRepository.findAllByRunIdOrderBySettlementCompletedDateAscIdAsc(runId);

        BigDecimal settlementAmount = BigDecimal.ZERO;
        BigDecimal generalSettlementAmount = BigDecimal.ZERO;
        BigDecimal fastSettlementAmount = BigDecimal.ZERO;
        BigDecimal settlementBaseAmount = BigDecimal.ZERO;
        BigDecimal totalFeeAmount = BigDecimal.ZERO;
        BigDecimal benefitSettlementAmount = BigDecimal.ZERO;
        BigDecimal dailyDeductionRefundAmount = BigDecimal.ZERO;
        BigDecimal holdAmount = BigDecimal.ZERO;
        BigDecimal bizWalletOffsetAmount = BigDecimal.ZERO;
        BigDecimal safeReturnCareCost = BigDecimal.ZERO;
        BigDecimal preferredFeeRefundAmount = BigDecimal.ZERO;

        for (SettlementDailyRaw row : dailyRows) {
            settlementAmount = settlementAmount.add(nvl(row.getSettlementAmount()));
            generalSettlementAmount = generalSettlementAmount.add(nvl(row.getGeneralSettlementAmount()));
            fastSettlementAmount = fastSettlementAmount.add(nvl(row.getFastSettlementAmount()));
            settlementBaseAmount = settlementBaseAmount.add(nvl(row.getSettlementBaseAmount()));
            totalFeeAmount = totalFeeAmount.add(nvl(row.getTotalFeeAmount()));
            benefitSettlementAmount = benefitSettlementAmount.add(nvl(row.getBenefitSettlementAmount()));
            dailyDeductionRefundAmount = dailyDeductionRefundAmount.add(nvl(row.getDailyDeductionRefundAmount()));
            holdAmount = holdAmount.add(nvl(row.getHoldAmount()));
            bizWalletOffsetAmount = bizWalletOffsetAmount.add(nvl(row.getBizWalletOffsetAmount()));
            safeReturnCareCost = safeReturnCareCost.add(nvl(row.getSafeReturnCareCost()));
            preferredFeeRefundAmount = preferredFeeRefundAmount.add(nvl(row.getPreferredFeeRefundAmount()));
        }

        return new SettlementRunSummaryResponse(
                dailyCount,
                monthlyCount,
                orderCount,
                feeCount,
                snapshotCount,
                issueCount,
                settlementAmount,
                generalSettlementAmount,
                fastSettlementAmount,
                settlementBaseAmount,
                totalFeeAmount,
                benefitSettlementAmount,
                dailyDeductionRefundAmount,
                holdAmount,
                bizWalletOffsetAmount,
                safeReturnCareCost,
                preferredFeeRefundAmount
        );
    }

    private BigDecimal nvl(BigDecimal value) {
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