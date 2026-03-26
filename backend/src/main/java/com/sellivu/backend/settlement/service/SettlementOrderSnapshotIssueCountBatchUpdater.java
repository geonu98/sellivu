package com.sellivu.backend.settlement.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SettlementOrderSnapshotIssueCountBatchUpdater {

    private final JdbcTemplate jdbcTemplate;

    public void rebuildIssueCounts(Long runId) {
        String resetSql = """
                UPDATE settlement_order_snapshot
                SET issue_count = 0,
                    last_aggregated_at = NOW()
                WHERE run_id = ?
                """;

        String aggregateSql = """
                UPDATE settlement_order_snapshot s
                SET issue_count = agg.issue_count,
                    last_aggregated_at = NOW()
                FROM (
                    SELECT snapshot_id, COUNT(*) AS issue_count
                    FROM settlement_issue
                    WHERE run_id = ?
                    GROUP BY snapshot_id
                ) agg
                WHERE s.id = agg.snapshot_id
                  AND s.run_id = ?
                """;

        jdbcTemplate.update(resetSql, runId);
        jdbcTemplate.update(aggregateSql, runId, runId);
    }
}