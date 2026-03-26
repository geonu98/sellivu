package com.sellivu.backend.settlement.controller;

import com.sellivu.backend.settlement.domain.SettlementWorkspace;
import com.sellivu.backend.settlement.dto.PageResponse;
import com.sellivu.backend.settlement.dto.SettlementDailyRowResponse;
import com.sellivu.backend.settlement.dto.SettlementFeeRowResponse;
import com.sellivu.backend.settlement.dto.SettlementIssueResponse;
import com.sellivu.backend.settlement.dto.SettlementMonthlySummaryResponse;
import com.sellivu.backend.settlement.dto.SettlementOrderRowResponse;
import com.sellivu.backend.settlement.dto.SettlementRunStartResponse;
import com.sellivu.backend.settlement.dto.SettlementRunSummaryResponse;
import com.sellivu.backend.settlement.dto.SettlementSnapshotResponse;
import com.sellivu.backend.settlement.service.SettlementAnalysisOrchestrator;
import com.sellivu.backend.settlement.service.SettlementRunQueryService;
import com.sellivu.backend.settlement.service.SettlementWorkspaceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/settlement/workspaces")
public class SettlementRunController {

    private final SettlementAnalysisOrchestrator settlementAnalysisOrchestrator;
    private final SettlementRunQueryService settlementRunQueryService;
    private final SettlementWorkspaceService settlementWorkspaceService;

    @PostMapping("/{workspaceKey}/runs/start")
    public SettlementRunStartResponse startRunByWorkspaceKey(
            @PathVariable String workspaceKey,
            @RequestHeader("X-Workspace-Token") String workspaceToken,
            @RequestParam(required = false) Long dailyUploadId,
            @RequestParam(required = false) Long orderUploadId,
            @RequestParam(required = false) Long feeUploadId
    ) {
        long totalStart = System.currentTimeMillis();

        SettlementWorkspace workspace =
                settlementWorkspaceService.validateAccessibleWorkspace(workspaceKey, workspaceToken);

        log.info(
                "[PERF] runController.start workspaceId={} workspaceKey={} dailyUploadId={} orderUploadId={} feeUploadId={}",
                workspace.getId(),
                workspaceKey,
                dailyUploadId,
                orderUploadId,
                feeUploadId
        );

        Long runId = settlementAnalysisOrchestrator.startRawLoading(
                workspace.getId(),
                dailyUploadId,
                orderUploadId,
                feeUploadId
        );

        log.info(
                "[PERF] runController.startRun total workspaceId={} runId={} took={}ms",
                workspace.getId(),
                runId,
                System.currentTimeMillis() - totalStart
        );

        return SettlementRunStartResponse.of(runId);
    }

    @GetMapping("/{workspaceKey}/active-run/snapshots")
    public PageResponse<SettlementSnapshotResponse> getActiveRunSnapshots(
            @PathVariable String workspaceKey,
            @RequestHeader("X-Workspace-Token") String workspaceToken,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size
    ) {
        long totalStart = System.currentTimeMillis();

        SettlementWorkspace workspace =
                settlementWorkspaceService.validateAccessibleWorkspace(workspaceKey, workspaceToken);

        Page<SettlementSnapshotResponse> result = settlementRunQueryService
                .getActiveRunSnapshots(workspace.getId(), page, size)
                .map(SettlementSnapshotResponse::from);

        log.info(
                "[PERF] runController.activeSnapshots workspaceId={} page={} size={} count={} total={} took={}ms",
                workspace.getId(),
                page,
                size,
                result.getNumberOfElements(),
                result.getTotalElements(),
                System.currentTimeMillis() - totalStart
        );

        return PageResponse.from(result);
    }

    @GetMapping("/{workspaceKey}/active-run/issues")
    public PageResponse<SettlementIssueResponse> getActiveRunIssues(
            @PathVariable String workspaceKey,
            @RequestHeader("X-Workspace-Token") String workspaceToken,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size
    ) {
        long totalStart = System.currentTimeMillis();

        SettlementWorkspace workspace =
                settlementWorkspaceService.validateAccessibleWorkspace(workspaceKey, workspaceToken);

        Page<SettlementIssueResponse> result = settlementRunQueryService
                .getActiveRunIssues(workspace.getId(), page, size)
                .map(SettlementIssueResponse::from);

        log.info(
                "[PERF] runController.activeIssues workspaceId={} page={} size={} count={} total={} took={}ms",
                workspace.getId(),
                page,
                size,
                result.getNumberOfElements(),
                result.getTotalElements(),
                System.currentTimeMillis() - totalStart
        );

        return PageResponse.from(result);
    }

    @GetMapping("/{workspaceKey}/active-run/orders")
    public PageResponse<SettlementOrderRowResponse> getActiveRunOrders(
            @PathVariable String workspaceKey,
            @RequestHeader("X-Workspace-Token") String workspaceToken,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size
    ) {
        long totalStart = System.currentTimeMillis();

        Page<SettlementOrderRowResponse> result =
                settlementRunQueryService.getActiveRunOrders(workspaceKey, workspaceToken, page, size);

        log.info(
                "[PERF] runController.activeOrders workspaceKey={} page={} size={} count={} total={} took={}ms",
                workspaceKey,
                page,
                size,
                result.getNumberOfElements(),
                result.getTotalElements(),
                System.currentTimeMillis() - totalStart
        );

        return PageResponse.from(result);
    }

    @GetMapping("/{workspaceKey}/active-run/fees")
    public PageResponse<SettlementFeeRowResponse> getActiveRunFees(
            @PathVariable String workspaceKey,
            @RequestHeader("X-Workspace-Token") String workspaceToken,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size
    ) {
        long totalStart = System.currentTimeMillis();

        Page<SettlementFeeRowResponse> result =
                settlementRunQueryService.getActiveRunFees(workspaceKey, workspaceToken, page, size);

        log.info(
                "[PERF] runController.activeFees workspaceKey={} page={} size={} count={} total={} took={}ms",
                workspaceKey,
                page,
                size,
                result.getNumberOfElements(),
                result.getTotalElements(),
                System.currentTimeMillis() - totalStart
        );

        return PageResponse.from(result);
    }

    @GetMapping("/{workspaceKey}/active-run/daily")
    public PageResponse<SettlementDailyRowResponse> getActiveRunDaily(
            @PathVariable String workspaceKey,
            @RequestHeader("X-Workspace-Token") String workspaceToken,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size
    ) {
        long totalStart = System.currentTimeMillis();

        Page<SettlementDailyRowResponse> result =
                settlementRunQueryService.getActiveRunDaily(workspaceKey, workspaceToken, page, size);

        log.info(
                "[PERF] runController.activeDaily workspaceKey={} page={} size={} count={} total={} took={}ms",
                workspaceKey,
                page,
                size,
                result.getNumberOfElements(),
                result.getTotalElements(),
                System.currentTimeMillis() - totalStart
        );

        return PageResponse.from(result);
    }

    @GetMapping("/{workspaceKey}/active-run/monthly")
    public List<SettlementMonthlySummaryResponse> getActiveRunMonthly(
            @PathVariable String workspaceKey,
            @RequestHeader("X-Workspace-Token") String workspaceToken
    ) {
        long totalStart = System.currentTimeMillis();

        List<SettlementMonthlySummaryResponse> result =
                settlementRunQueryService.getActiveRunMonthly(workspaceKey, workspaceToken);

        log.info(
                "[PERF] runController.activeMonthly workspaceKey={} count={} took={}ms",
                workspaceKey,
                result.size(),
                System.currentTimeMillis() - totalStart
        );

        return result;
    }

    @GetMapping("/{workspaceKey}/active-run/summary")
    public SettlementRunSummaryResponse getActiveRunSummary(
            @PathVariable String workspaceKey,
            @RequestHeader("X-Workspace-Token") String workspaceToken
    ) {
        long totalStart = System.currentTimeMillis();

        SettlementRunSummaryResponse result =
                settlementRunQueryService.getActiveRunSummary(workspaceKey, workspaceToken);

        log.info(
                "[PERF] runController.activeSummary workspaceKey={} dailyCount={} monthlyCount={} orderCount={} feeCount={} snapshotCount={} issueCount={} took={}ms",
                workspaceKey,
                result.getDailyCount(),
                result.getMonthlyCount(),
                result.getOrderCount(),
                result.getFeeCount(),
                result.getSnapshotCount(),
                result.getIssueCount(),
                System.currentTimeMillis() - totalStart
        );

        return result;
    }
}