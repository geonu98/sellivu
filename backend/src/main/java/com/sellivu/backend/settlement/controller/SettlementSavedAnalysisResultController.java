package com.sellivu.backend.settlement.controller;

import com.sellivu.backend.settlement.dto.SettlementAnalysisIssueResponse;
import com.sellivu.backend.settlement.dto.SettlementDailyRowResponse;
import com.sellivu.backend.settlement.dto.SettlementFeeRowResponse;
import com.sellivu.backend.settlement.dto.SettlementMonthlySummaryResponse;
import com.sellivu.backend.settlement.dto.SettlementOrderRowResponse;
import com.sellivu.backend.settlement.dto.SettlementSnapshotResponse;
import com.sellivu.backend.settlement.service.SettlementAnalysisDailyService;
import com.sellivu.backend.settlement.service.SettlementAnalysisFeeService;
import com.sellivu.backend.settlement.service.SettlementAnalysisIssueService;
import com.sellivu.backend.settlement.service.SettlementAnalysisMonthlyService;
import com.sellivu.backend.settlement.service.SettlementAnalysisOrderService;
import com.sellivu.backend.settlement.service.SettlementAnalysisSetService;
import com.sellivu.backend.settlement.service.SettlementAnalysisSnapshotService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/settlement/analysis-sets/my/{analysisSetId}")
public class SettlementSavedAnalysisResultController {

    private final SettlementAnalysisSetService settlementAnalysisSetService;
    private final SettlementAnalysisIssueService settlementAnalysisIssueService;
    private final SettlementAnalysisSnapshotService settlementAnalysisSnapshotService;
    private final SettlementAnalysisDailyService settlementAnalysisDailyService;
    private final SettlementAnalysisMonthlyService settlementAnalysisMonthlyService;
    private final SettlementAnalysisOrderService settlementAnalysisOrderService;
    private final SettlementAnalysisFeeService settlementAnalysisFeeService;

    @GetMapping("/issues")
    public List<SettlementAnalysisIssueResponse> getIssues(
            @PathVariable Long analysisSetId
    ) {
        settlementAnalysisSetService.validateMySavedSetAccessible(analysisSetId, null);
        return settlementAnalysisIssueService.getIssues(analysisSetId);
    }

    @GetMapping("/snapshots")
    public List<SettlementSnapshotResponse> getSnapshots(
            @PathVariable Long analysisSetId
    ) {
        settlementAnalysisSetService.validateMySavedSetAccessible(analysisSetId, null);
        return settlementAnalysisSnapshotService.getSnapshots(analysisSetId);
    }

    @GetMapping("/daily")
    public List<SettlementDailyRowResponse> getDailyRows(
            @PathVariable Long analysisSetId
    ) {
        settlementAnalysisSetService.validateMySavedSetAccessible(analysisSetId, null);
        return settlementAnalysisDailyService.getDailyRows(analysisSetId);
    }

    @GetMapping("/monthly")
    public List<SettlementMonthlySummaryResponse> getMonthlySummaries(
            @PathVariable Long analysisSetId
    ) {
        settlementAnalysisSetService.validateMySavedSetAccessible(analysisSetId, null);
        return settlementAnalysisMonthlyService.getMonthlySummaries(analysisSetId);
    }

    @GetMapping("/orders")
    public List<SettlementOrderRowResponse> getOrderRows(
            @PathVariable Long analysisSetId
    ) {
        settlementAnalysisSetService.validateMySavedSetAccessible(analysisSetId, null);
        return settlementAnalysisOrderService.getOrderRows(analysisSetId);
    }

    @GetMapping("/fees")
    public List<SettlementFeeRowResponse> getFeeRows(
            @PathVariable Long analysisSetId
    ) {
        settlementAnalysisSetService.validateMySavedSetAccessible(analysisSetId, null);
        return settlementAnalysisFeeService.getFeeRows(analysisSetId);
    }
}