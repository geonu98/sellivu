package com.sellivu.backend.settlement.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementAnalysisStepService {

    private final SettlementAnalysisRunService settlementAnalysisRunService;
    private final SettlementRawLoadService settlementRawLoadService;
    private final SettlementSnapshotBuildService settlementSnapshotBuildService;
    private final SettlementIssueBuildService settlementIssueBuildService;

    @Transactional
    public void markRawLoading(Long runId) {
        long start = System.currentTimeMillis();
        settlementAnalysisRunService.markRawLoading(runId);
        log.info("[PERF] step.markRawLoading runId={} took={}ms",
                runId, System.currentTimeMillis() - start);
    }

    @Transactional
    public int loadDailyRaw(Long runId, Long dailyUploadId) {
        long start = System.currentTimeMillis();
        int count = settlementRawLoadService.loadDailyRaw(runId, dailyUploadId);
        log.info("[PERF] step.loadDailyRaw runId={} dailyUploadId={} rows={} took={}ms",
                runId, dailyUploadId, count, System.currentTimeMillis() - start);
        return count;
    }

    @Transactional
    public int loadOrderRaw(Long runId, Long orderUploadId) {
        long start = System.currentTimeMillis();
        int count = settlementRawLoadService.loadOrderRaw(runId, orderUploadId);
        log.info("[PERF] step.loadOrderRaw runId={} orderUploadId={} rows={} took={}ms",
                runId, orderUploadId, count, System.currentTimeMillis() - start);
        return count;
    }

    @Transactional
    public int loadFeeRaw(Long runId, Long feeUploadId) {
        long start = System.currentTimeMillis();
        int count = settlementRawLoadService.loadFeeRaw(runId, feeUploadId);
        log.info("[PERF] step.loadFeeRaw runId={} feeUploadId={} rows={} took={}ms",
                runId, feeUploadId, count, System.currentTimeMillis() - start);
        return count;
    }

    @Transactional
    public void updateRowCounts(Long runId, int dailyRawCount, int orderRawCount, int feeRawCount) {
        long start = System.currentTimeMillis();
        settlementAnalysisRunService.updateRowCounts(runId, dailyRawCount, orderRawCount, feeRawCount);
        log.info("[PERF] step.updateRowCounts runId={} daily={} order={} fee={} took={}ms",
                runId, dailyRawCount, orderRawCount, feeRawCount, System.currentTimeMillis() - start);
    }

    @Transactional
    public void markAnalyzing(Long runId) {
        long start = System.currentTimeMillis();
        settlementAnalysisRunService.markAnalyzing(runId);
        log.info("[PERF] step.markAnalyzing runId={} took={}ms",
                runId, System.currentTimeMillis() - start);
    }

    @Transactional
    public int buildSnapshots(Long runId) {
        long start = System.currentTimeMillis();
        int snapshotCount = settlementSnapshotBuildService.build(runId);
        log.info("[PERF] step.buildSnapshots runId={} snapshotCount={} took={}ms",
                runId, snapshotCount, System.currentTimeMillis() - start);
        return snapshotCount;
    }

    public int buildIssues(Long runId) {
        long start = System.currentTimeMillis();
        int issueCount = settlementIssueBuildService.build(runId);
        log.info("[PERF] step.buildIssues runId={} issueCount={} took={}ms",
                runId, issueCount, System.currentTimeMillis() - start);
        return issueCount;
    }

    @Transactional
    public void markCompleted(Long runId, int snapshotCount, int issueCount) {
        long start = System.currentTimeMillis();
        settlementAnalysisRunService.markCompleted(runId, snapshotCount, issueCount);
        log.info("[PERF] step.markCompleted runId={} snapshotCount={} issueCount={} took={}ms",
                runId, snapshotCount, issueCount, System.currentTimeMillis() - start);
    }

    @Transactional
    public void activateRun(Long workspaceId, Long runId) {
        long start = System.currentTimeMillis();
        settlementAnalysisRunService.activateRun(workspaceId, runId);
        log.info("[PERF] step.activateRun workspaceId={} runId={} took={}ms",
                workspaceId, runId, System.currentTimeMillis() - start);
    }

    @Transactional
    public void markFailed(Long runId, String message) {
        long start = System.currentTimeMillis();
        settlementAnalysisRunService.markFailed(runId, message);
        log.info("[PERF] step.markFailed runId={} took={}ms",
                runId, System.currentTimeMillis() - start);
    }
}