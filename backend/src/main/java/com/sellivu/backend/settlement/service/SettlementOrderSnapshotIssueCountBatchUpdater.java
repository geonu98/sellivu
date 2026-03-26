package com.sellivu.backend.settlement.service;

import com.sellivu.backend.settlement.domain.SettlementOrderSnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SettlementOrderSnapshotIssueCountBatchUpdater {

    private static final int BATCH_SIZE = 1000;

    private final JdbcTemplate jdbcTemplate;

    public void updateIssueCounts(Long runId, List<SettlementOrderSnapshot> snapshots) {
        if (snapshots == null || snapshots.isEmpty()) {
            return;
        }

        String sql = """
                UPDATE settlement_order_snapshot
                SET issue_count = ?, last_aggregated_at = NOW()
                WHERE id = ? AND run_id = ?
                """;

        for (int start = 0; start < snapshots.size(); start += BATCH_SIZE) {
            int end = Math.min(start + BATCH_SIZE, snapshots.size());
            List<SettlementOrderSnapshot> batch = snapshots.subList(start, end);

            jdbcTemplate.batchUpdate(sql, batch, batch.size(), (ps, item) -> {
                ps.setInt(1, item.getIssueCount());
                ps.setLong(2, item.getId());
                ps.setLong(3, runId);
            });
        }
    }
}