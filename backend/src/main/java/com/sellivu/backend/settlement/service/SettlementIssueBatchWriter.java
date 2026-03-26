package com.sellivu.backend.settlement.service;

import com.sellivu.backend.settlement.domain.SettlementIssue;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SettlementIssueBatchWriter {

    private static final int BATCH_SIZE = 1000;

    private final JdbcTemplate jdbcTemplate;

    public void insertBatch(List<SettlementIssue> issues) {
        if (issues == null || issues.isEmpty()) {
            return;
        }

        String sql = """
                INSERT INTO settlement_issue (
                    run_id,
                    snapshot_id,
                    issue_type,
                    order_no,
                    product_order_no,
                    join_key,
                    message,
                    resolved,
                    severity,
                    judgement_status,
                    explanation_code,
                    needs_user_input,
                    created_at
                ) VALUES (
                    ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?
                )
                """;

        for (int start = 0; start < issues.size(); start += BATCH_SIZE) {
            int end = Math.min(start + BATCH_SIZE, issues.size());
            List<SettlementIssue> batch = issues.subList(start, end);

            jdbcTemplate.batchUpdate(sql, batch, batch.size(), (ps, item) -> {
                ps.setObject(1, item.getRunId(), Types.BIGINT);
                ps.setObject(2, item.getSnapshotId(), Types.BIGINT);
                ps.setString(3, item.getIssueType() != null ? item.getIssueType().name() : null);
                ps.setString(4, item.getOrderNo());
                ps.setString(5, item.getProductOrderNo());
                ps.setString(6, item.getJoinKey());
                ps.setString(7, item.getMessage());
                ps.setBoolean(8, item.isResolved());
                ps.setString(9, item.getSeverity() != null ? item.getSeverity().name() : null);
                ps.setString(10, item.getJudgementStatus() != null ? item.getJudgementStatus().name() : null);
                ps.setString(11, item.getExplanationCode() != null ? item.getExplanationCode().name() : null);
                ps.setBoolean(12, item.isNeedsUserInput());

                if (item.getCreatedAt() != null) {
                    ps.setTimestamp(13, Timestamp.valueOf(item.getCreatedAt()));
                } else {
                    ps.setNull(13, Types.TIMESTAMP);
                }
            });
        }
    }
}