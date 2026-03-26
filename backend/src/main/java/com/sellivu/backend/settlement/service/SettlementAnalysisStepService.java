package com.sellivu.backend.settlement.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SettlementAnalysisStepService {

    private final SettlementAnalysisRunService settlementAnalysisRunService;
    private final SettlementRawLoadService settlementRawLoadService;
    private final SettlementSnapshotBuildService settlementSnapshotBuildService;
    private final SettlementIssueBuildService settlementIssueBuildService;

    @Transactional
    public void markRawLoading(Long runId) {
        settlementAnalysisRunService.markRawLoading(runId);
    }

    @Transactional
    public int loadDailyRaw(Long runId, Long dailyUploadId) {
        return settlementRawLoadService.loadDailyRaw(runId, dailyUploadId);
    }

    @Transactional
    public int loadOrderRaw(Long runId, Long orderUploadId) {
        return settlementRawLoadService.loadOrderRaw(runId, orderUploadId);
    }

    @Transactional
    public int loadFeeRaw(Long runId, Long feeUploadId) {
        return settlementRawLoadService.loadFeeRaw(runId, feeUploadId);
    }

    @Transactional
    public void updateRowCounts(Long runId, int dailyRawCount, int orderRawCount, int feeRawCount) {
        settlementAnalysisRunService.updateRowCounts(runId, dailyRawCount, orderRawCount, feeRawCount);
    }

    @Transactional
    public void markAnalyzing(Long runId) {
        settlementAnalysisRunService.markAnalyzing(runId);
    }

    @Transactional
    public int buildSnapshots(Long runId) {
        return settlementSnapshotBuildService.build(runId);
    }

    public int buildIssues(Long runId) {
        return settlementIssueBuildService.build(runId);
    }

    @Transactional
    public void markCompleted(Long runId, int snapshotCount, int issueCount) {
        settlementAnalysisRunService.markCompleted(runId, snapshotCount, issueCount);
    }

    @Transactional
    public void activateRun(Long workspaceId, Long runId) {
        settlementAnalysisRunService.activateRun(workspaceId, runId);
    }

    @Transactional
    public void markFailed(Long runId, String message) {
        settlementAnalysisRunService.markFailed(runId, message);
    }
}