package com.sellivu.backend.settlement.service;

import com.sellivu.backend.settlement.domain.SettlementIssue;
import com.sellivu.backend.settlement.repository.SettlementIssueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SettlementIssueBuildStepService {

    private final SettlementIssueRepository settlementIssueRepository;
    private final SettlementIssueBatchWriter settlementIssueBatchWriter;
    private final SettlementOrderSnapshotIssueCountBatchUpdater settlementOrderSnapshotIssueCountBatchUpdater;

    @Transactional
    public void deleteIssuesByRunId(Long runId) {
        settlementIssueRepository.deleteAllByRunId(runId);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void insertIssueChunk(List<SettlementIssue> issues) {
        if (issues == null || issues.isEmpty()) {
            return;
        }
        settlementIssueBatchWriter.insertBatch(issues);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void rebuildSnapshotIssueCounts(Long runId) {
        settlementOrderSnapshotIssueCountBatchUpdater.rebuildIssueCounts(runId);
    }
}