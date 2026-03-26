package com.sellivu.backend.settlement.service;

import com.sellivu.backend.settlement.domain.IssueExplanationCode;
import com.sellivu.backend.settlement.domain.IssueJudgementStatus;
import com.sellivu.backend.settlement.domain.IssueSeverity;
import com.sellivu.backend.settlement.domain.MatchStatus;
import com.sellivu.backend.settlement.domain.SettlementIssue;
import com.sellivu.backend.settlement.domain.SettlementIssueType;
import com.sellivu.backend.settlement.domain.SettlementOrderSnapshot;
import com.sellivu.backend.settlement.repository.SettlementOrderSnapshotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementIssueBuildService {

    private static final int ISSUE_CHUNK_SIZE = 1000;
    private static final int SNAPSHOT_UPDATE_CHUNK_SIZE = 1000;

    private final SettlementOrderSnapshotRepository settlementOrderSnapshotRepository;
    private final SettlementIssueBuildStepService settlementIssueBuildStepService;

    public int build(Long runId) {
        long totalStartedAt = System.currentTimeMillis();

        long deleteStartedAt = System.currentTimeMillis();
        settlementIssueBuildStepService.deleteIssuesByRunId(runId);
        log.info("[PERF] issue delete runId={} took={}ms",
                runId,
                System.currentTimeMillis() - deleteStartedAt
        );

        long loadStartedAt = System.currentTimeMillis();
        List<SettlementOrderSnapshot> snapshots =
                settlementOrderSnapshotRepository.findAllByRunIdOrderByIdAsc(runId);
        log.info("[PERF] issue snapshot load runId={} snapshots={} took={}ms",
                runId,
                snapshots.size(),
                System.currentTimeMillis() - loadStartedAt
        );

        long buildStartedAt = System.currentTimeMillis();
        List<SettlementIssue> issues = new ArrayList<>();

        for (SettlementOrderSnapshot snapshot : snapshots) {
            List<SettlementIssue> snapshotIssues = buildIssues(snapshot, runId);
            snapshot.updateIssueCount(snapshotIssues.size());
            issues.addAll(snapshotIssues);
        }

        log.info("[PERF] issue entity build runId={} issues={} took={}ms",
                runId,
                issues.size(),
                System.currentTimeMillis() - buildStartedAt
        );

        long issueSaveStartedAt = System.currentTimeMillis();
        int insertedCount = 0;

        for (int start = 0; start < issues.size(); start += ISSUE_CHUNK_SIZE) {
            int end = Math.min(start + ISSUE_CHUNK_SIZE, issues.size());
            List<SettlementIssue> chunk = issues.subList(start, end);

            long chunkStartedAt = System.currentTimeMillis();
            settlementIssueBuildStepService.insertIssueChunk(chunk);
            insertedCount += chunk.size();

            log.info("[PERF] issue chunk insert runId={} start={} end={} size={} took={}ms",
                    runId,
                    start,
                    end,
                    chunk.size(),
                    System.currentTimeMillis() - chunkStartedAt
            );
        }

        log.info("[PERF] issue batch insert runId={} issues={} took={}ms",
                runId,
                insertedCount,
                System.currentTimeMillis() - issueSaveStartedAt
        );

        long issueCountUpdateStartedAt = System.currentTimeMillis();
        int updatedSnapshotCount = 0;

        for (int start = 0; start < snapshots.size(); start += SNAPSHOT_UPDATE_CHUNK_SIZE) {
            int end = Math.min(start + SNAPSHOT_UPDATE_CHUNK_SIZE, snapshots.size());
            List<SettlementOrderSnapshot> chunk = snapshots.subList(start, end);

            long chunkStartedAt = System.currentTimeMillis();
            settlementIssueBuildStepService.updateSnapshotIssueCountChunk(runId, chunk);
            updatedSnapshotCount += chunk.size();

            log.info("[PERF] snapshot issueCount chunk update runId={} start={} end={} size={} took={}ms",
                    runId,
                    start,
                    end,
                    chunk.size(),
                    System.currentTimeMillis() - chunkStartedAt
            );
        }

        log.info("[PERF] snapshot issueCount batch update runId={} snapshots={} took={}ms",
                runId,
                updatedSnapshotCount,
                System.currentTimeMillis() - issueCountUpdateStartedAt
        );

        log.info("[PERF] issue total runId={} total={}ms",
                runId,
                System.currentTimeMillis() - totalStartedAt
        );

        return issues.size();
    }

    private List<SettlementIssue> buildIssues(SettlementOrderSnapshot snapshot, Long runId) {
        List<SettlementIssue> issues = new ArrayList<>();

        MatchStatus matchStatus = snapshot.getMatchStatus();

        if (matchStatus == MatchStatus.ORDER_ONLY) {
            issues.add(createIssue(
                    runId,
                    snapshot,
                    SettlementIssueType.ORDER_ROW_UNMATCHED,
                    "주문 정산 데이터만 있고 수수료 데이터가 없습니다."
            ));
        }

        if (matchStatus == MatchStatus.FEE_ONLY) {
            issues.add(createIssue(
                    runId,
                    snapshot,
                    SettlementIssueType.FEE_ROW_UNMATCHED,
                    "수수료 데이터만 있고 주문 정산 데이터가 없습니다."
            ));
        }

        if (!snapshot.isSettlementAmountMatched()) {
            issues.add(createIssue(
                    runId,
                    snapshot,
                    SettlementIssueType.SETTLEMENT_AMOUNT_MISMATCH,
                    "정산 금액이 일치하지 않습니다."
            ));
        }

        if (!snapshot.isCommissionAmountMatched()) {
            issues.add(createIssue(
                    runId,
                    snapshot,
                    SettlementIssueType.COMMISSION_AMOUNT_MISMATCH,
                    "수수료 금액이 일치하지 않습니다."
            ));
        }

        if (!snapshot.isNetAmountMatched()) {
            issues.add(createIssue(
                    runId,
                    snapshot,
                    SettlementIssueType.NET_AMOUNT_MISMATCH,
                    "실수령 금액이 일치하지 않습니다."
            ));
        }

        return issues;
    }

    private SettlementIssue createIssue(
            Long runId,
            SettlementOrderSnapshot snapshot,
            SettlementIssueType issueType,
            String message
    ) {
        SettlementIssue issue = SettlementIssue.createDetailed(
                snapshot.getId(),
                issueType,
                snapshot.getOrderNo(),
                snapshot.getProductOrderNo(),
                snapshot.getJoinKey(),
                message,
                IssueSeverity.ERROR,
                IssueJudgementStatus.CONFIRMED,
                (IssueExplanationCode) null,
                false
        );
        issue.assignRunId(runId);
        return issue;
    }
}