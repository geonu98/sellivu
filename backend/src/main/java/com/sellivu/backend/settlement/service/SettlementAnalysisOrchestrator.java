package com.sellivu.backend.settlement.service;

import com.sellivu.backend.settlement.domain.SettlementAnalysisRun;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementAnalysisOrchestrator {

    private final SettlementAnalysisRunService settlementAnalysisRunService;
    private final SettlementAnalysisStepService settlementAnalysisStepService;

    public Long startRawLoading(
            Long workspaceId,
            Long dailyUploadId,
            Long orderUploadId,
            Long feeUploadId
    ) {
        long totalStart = System.currentTimeMillis();

        log.info(
                "[PERF] orchestrator.start workspaceId={} dailyUploadId={} orderUploadId={} feeUploadId={}",
                workspaceId,
                dailyUploadId,
                orderUploadId,
                feeUploadId
        );

        long createRunStart = System.currentTimeMillis();
        SettlementAnalysisRun run = settlementAnalysisRunService.createRun(
                workspaceId,
                dailyUploadId,
                orderUploadId,
                feeUploadId
        );
        log.info(
                "[PERF] orchestrator.createRun workspaceId={} runId={} took={}ms",
                workspaceId,
                run.getId(),
                System.currentTimeMillis() - createRunStart
        );

        Long runId = run.getId();

        try {
            long markRawLoadingStart = System.currentTimeMillis();
            settlementAnalysisStepService.markRawLoading(runId);
            log.info(
                    "[PERF] orchestrator.markRawLoading runId={} took={}ms",
                    runId,
                    System.currentTimeMillis() - markRawLoadingStart
            );

            int dailyRawCount = 0;
            int orderRawCount = 0;
            int feeRawCount = 0;

            if (dailyUploadId != null) {
                long loadDailyRawStart = System.currentTimeMillis();
                dailyRawCount = settlementAnalysisStepService.loadDailyRaw(runId, dailyUploadId);
                log.info(
                        "[PERF] orchestrator.loadDailyRaw runId={} dailyUploadId={} count={} took={}ms",
                        runId,
                        dailyUploadId,
                        dailyRawCount,
                        System.currentTimeMillis() - loadDailyRawStart
                );
            }

            if (orderUploadId != null) {
                long loadOrderRawStart = System.currentTimeMillis();
                orderRawCount = settlementAnalysisStepService.loadOrderRaw(runId, orderUploadId);
                log.info(
                        "[PERF] orchestrator.loadOrderRaw runId={} orderUploadId={} count={} took={}ms",
                        runId,
                        orderUploadId,
                        orderRawCount,
                        System.currentTimeMillis() - loadOrderRawStart
                );
            }

            if (feeUploadId != null) {
                long loadFeeRawStart = System.currentTimeMillis();
                feeRawCount = settlementAnalysisStepService.loadFeeRaw(runId, feeUploadId);
                log.info(
                        "[PERF] orchestrator.loadFeeRaw runId={} feeUploadId={} count={} took={}ms",
                        runId,
                        feeUploadId,
                        feeRawCount,
                        System.currentTimeMillis() - loadFeeRawStart
                );
            }

            long updateRowCountsStart = System.currentTimeMillis();
            settlementAnalysisStepService.updateRowCounts(
                    runId,
                    dailyRawCount,
                    orderRawCount,
                    feeRawCount
            );
            log.info(
                    "[PERF] orchestrator.updateRowCounts runId={} dailyRawCount={} orderRawCount={} feeRawCount={} took={}ms",
                    runId,
                    dailyRawCount,
                    orderRawCount,
                    feeRawCount,
                    System.currentTimeMillis() - updateRowCountsStart
            );

            long markAnalyzingStart = System.currentTimeMillis();
            settlementAnalysisStepService.markAnalyzing(runId);
            log.info(
                    "[PERF] orchestrator.markAnalyzing runId={} took={}ms",
                    runId,
                    System.currentTimeMillis() - markAnalyzingStart
            );

            long snapshotBuildStart = System.currentTimeMillis();
            int snapshotCount = settlementAnalysisStepService.buildSnapshots(runId);
            log.info(
                    "[PERF] orchestrator.buildSnapshots runId={} snapshotCount={} took={}ms",
                    runId,
                    snapshotCount,
                    System.currentTimeMillis() - snapshotBuildStart
            );

            long markCompletedStart = System.currentTimeMillis();
            settlementAnalysisStepService.markCompleted(runId, snapshotCount);
            log.info(
                    "[PERF] orchestrator.markCompleted runId={} snapshotCount={} took={}ms",
                    runId,
                    snapshotCount,
                    System.currentTimeMillis() - markCompletedStart
            );

            long activateRunStart = System.currentTimeMillis();
            settlementAnalysisStepService.activateRun(workspaceId, runId);
            log.info(
                    "[PERF] orchestrator.activateRun workspaceId={} runId={} took={}ms",
                    workspaceId,
                    runId,
                    System.currentTimeMillis() - activateRunStart
            );

            log.info(
                    "[PERF] orchestrator.total workspaceId={} runId={} took={}ms",
                    workspaceId,
                    runId,
                    System.currentTimeMillis() - totalStart
            );

            return runId;
        } catch (Exception e) {
            long markFailedStart = System.currentTimeMillis();
            settlementAnalysisStepService.markFailed(runId, e.getMessage());
            log.info(
                    "[PERF] orchestrator.markFailed runId={} took={}ms",
                    runId,
                    System.currentTimeMillis() - markFailedStart
            );

            log.error(
                    "[PERF] orchestrator.failed workspaceId={} runId={} totalBeforeThrow={}ms message={}",
                    workspaceId,
                    runId,
                    System.currentTimeMillis() - totalStart,
                    e.getMessage(),
                    e
            );
            throw e;
        }
    }
}
