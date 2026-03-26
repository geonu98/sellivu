package com.sellivu.backend.settlement.service;

import com.sellivu.backend.global.error.ApiException;
import com.sellivu.backend.global.error.ErrorCode;
import com.sellivu.backend.settlement.domain.SettlementAnalysisRun;
import com.sellivu.backend.settlement.domain.SettlementWorkspace;
import com.sellivu.backend.settlement.repository.SettlementAnalysisRunRepository;
import com.sellivu.backend.settlement.repository.SettlementWorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SettlementAnalysisRunService {

    private final SettlementAnalysisRunRepository settlementAnalysisRunRepository;
    private final SettlementWorkspaceRepository settlementWorkspaceRepository;

    public SettlementAnalysisRun createRun(
            Long workspaceId,
            Long dailyUploadId,
            Long orderUploadId,
            Long feeUploadId
    ) {
        SettlementAnalysisRun run = SettlementAnalysisRun.create(
                workspaceId,
                dailyUploadId,
                orderUploadId,
                feeUploadId
        );
        return settlementAnalysisRunRepository.save(run);
    }

    public SettlementAnalysisRun getRun(Long runId) {
        return settlementAnalysisRunRepository.findById(runId)
                .orElseThrow(() -> new IllegalArgumentException("분석 실행 정보를 찾을 수 없습니다. runId=" + runId));
    }

    public void markRawLoading(Long runId) {
        SettlementAnalysisRun run = getRun(runId);
        run.markRawLoading();
    }

    public void markAnalyzing(Long runId) {
        SettlementAnalysisRun run = getRun(runId);
        run.markAnalyzing();
    }

    public void markCompleted(Long runId, int snapshotCount, int issueCount) {
        SettlementAnalysisRun run = getRun(runId);
        run.markCompleted(snapshotCount, issueCount);
    }

    public void markFailed(Long runId, String errorMessage) {
        SettlementAnalysisRun run = getRun(runId);
        run.markFailed(errorMessage);
    }

    public void updateRowCounts(Long runId, int dailyRowCount, int orderRowCount, int feeRowCount) {
        SettlementAnalysisRun run = getRun(runId);
        run.updateRowCounts(dailyRowCount, orderRowCount, feeRowCount);
    }

    public void activateRun(Long workspaceId, Long runId) {
        SettlementWorkspace workspace = settlementWorkspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ApiException(ErrorCode.WORKSPACE_NOT_FOUND));

        workspace.activateRun(runId);
    }
}