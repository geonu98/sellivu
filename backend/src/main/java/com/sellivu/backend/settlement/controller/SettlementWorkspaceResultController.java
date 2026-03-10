package com.sellivu.backend.settlement.controller;

import com.sellivu.backend.settlement.dto.SettlementDailyRowResponse;
import com.sellivu.backend.settlement.dto.SettlementFeeRowResponse;
import com.sellivu.backend.settlement.dto.SettlementMonthlySummaryResponse;
import com.sellivu.backend.settlement.dto.SettlementOrderRowResponse;
import com.sellivu.backend.settlement.dto.SettlementSnapshotResponse;
import com.sellivu.backend.settlement.service.SettlementAnalysisDailyService;
import com.sellivu.backend.settlement.service.SettlementAnalysisFeeService;
import com.sellivu.backend.settlement.service.SettlementAnalysisMonthlyService;
import com.sellivu.backend.settlement.service.SettlementAnalysisOrderService;
import com.sellivu.backend.settlement.service.SettlementAnalysisSnapshotService;
import com.sellivu.backend.workspace.service.WorkspaceAnalysisResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/settlement/workspaces")
public class SettlementWorkspaceResultController {

    private final WorkspaceAnalysisResolver workspaceAnalysisResolver;
    private final SettlementAnalysisSnapshotService settlementAnalysisSnapshotService;
    private final SettlementAnalysisDailyService settlementAnalysisDailyService;
    private final SettlementAnalysisMonthlyService settlementAnalysisMonthlyService;
    private final SettlementAnalysisOrderService settlementAnalysisOrderService;
    private final SettlementAnalysisFeeService settlementAnalysisFeeService;

    @GetMapping("/{workspaceKey}/snapshots")
    public List<SettlementSnapshotResponse> getSnapshots(
            @PathVariable String workspaceKey,
            @RequestHeader("X-Workspace-Token") String workspaceToken
    ) {
        Long analysisSetId = workspaceAnalysisResolver.resolveAnalysisSetId(workspaceKey, workspaceToken);
        return settlementAnalysisSnapshotService.getSnapshots(analysisSetId);
    }

    @GetMapping("/{workspaceKey}/daily")
    public List<SettlementDailyRowResponse> getDailyRows(
            @PathVariable String workspaceKey,
            @RequestHeader("X-Workspace-Token") String workspaceToken
    ) {
        Long analysisSetId = workspaceAnalysisResolver.resolveAnalysisSetId(workspaceKey, workspaceToken);
        return settlementAnalysisDailyService.getDailyRows(analysisSetId);
    }

    @GetMapping("/{workspaceKey}/monthly")
    public List<SettlementMonthlySummaryResponse> getMonthlySummaries(
            @PathVariable String workspaceKey,
            @RequestHeader("X-Workspace-Token") String workspaceToken
    ) {
        Long analysisSetId = workspaceAnalysisResolver.resolveAnalysisSetId(workspaceKey, workspaceToken);
        return settlementAnalysisMonthlyService.getMonthlySummaries(analysisSetId);
    }

    @GetMapping("/{workspaceKey}/orders")
    public List<SettlementOrderRowResponse> getOrderRows(
            @PathVariable String workspaceKey,
            @RequestHeader("X-Workspace-Token") String workspaceToken
    ) {
        Long analysisSetId = workspaceAnalysisResolver.resolveAnalysisSetId(workspaceKey, workspaceToken);
        return settlementAnalysisOrderService.getOrderRows(analysisSetId);
    }

    @GetMapping("/{workspaceKey}/fees")
    public List<SettlementFeeRowResponse> getFeeRows(
            @PathVariable String workspaceKey,
            @RequestHeader("X-Workspace-Token") String workspaceToken
    ) {
        Long analysisSetId = workspaceAnalysisResolver.resolveAnalysisSetId(workspaceKey, workspaceToken);
        return settlementAnalysisFeeService.getFeeRows(analysisSetId);
    }
}