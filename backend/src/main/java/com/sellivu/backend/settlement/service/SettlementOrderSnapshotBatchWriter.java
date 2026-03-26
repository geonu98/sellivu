package com.sellivu.backend.settlement.service;

import com.sellivu.backend.settlement.domain.SettlementOrderSnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SettlementOrderSnapshotBatchWriter {

    private static final int BATCH_SIZE = 1000;

    private final JdbcTemplate jdbcTemplate;

    public void insertBatch(List<SettlementOrderSnapshot> snapshots) {
        if (snapshots == null || snapshots.isEmpty()) {
            return;
        }

        String sql = """
                INSERT INTO settlement_order_snapshot (
                    run_id,
                    join_key,
                    order_no,
                    product_order_no,
                    match_status,
                    order_row_id,
                    fee_row_id,
                    order_upload_id,
                    fee_upload_id,
                    product_name,
                    option_name,
                    seller_product_code,
                    seller_option_code,
                    paid_at,
                    settlement_date,
                    order_settlement_amount,
                    order_commission_amount,
                    order_net_amount,
                    fee_settlement_amount,
                    fee_commission_amount,
                    fee_net_amount,
                    resolved_settlement_amount,
                    resolved_commission_amount,
                    resolved_net_amount,
                    settlement_amount_matched,
                    commission_amount_matched,
                    net_amount_matched,
                    issue_count,
                    last_aggregated_at
                ) VALUES (
                    ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?
                )
                """;

        for (int start = 0; start < snapshots.size(); start += BATCH_SIZE) {
            int end = Math.min(start + BATCH_SIZE, snapshots.size());
            List<SettlementOrderSnapshot> batch = snapshots.subList(start, end);

            jdbcTemplate.batchUpdate(sql, batch, batch.size(), (ps, item) -> {
                ps.setObject(1, item.getRunId(), Types.BIGINT);
                ps.setString(2, item.getJoinKey());
                ps.setString(3, item.getOrderNo());
                ps.setString(4, item.getProductOrderNo());
                ps.setString(5, item.getMatchStatus() != null ? item.getMatchStatus().name() : null);
                ps.setObject(6, item.getOrderRowId(), Types.BIGINT);
                ps.setObject(7, item.getFeeRowId(), Types.BIGINT);
                ps.setObject(8, item.getOrderUploadId(), Types.BIGINT);
                ps.setObject(9, item.getFeeUploadId(), Types.BIGINT);
                ps.setString(10, item.getProductName());
                ps.setString(11, item.getOptionName());
                ps.setString(12, item.getSellerProductCode());
                ps.setString(13, item.getSellerOptionCode());

                if (item.getPaidAt() != null) {
                    ps.setDate(14, Date.valueOf(item.getPaidAt()));
                } else {
                    ps.setNull(14, Types.DATE);
                }

                if (item.getSettlementDate() != null) {
                    ps.setDate(15, Date.valueOf(item.getSettlementDate()));
                } else {
                    ps.setNull(15, Types.DATE);
                }

                ps.setBigDecimal(16, item.getOrderSettlementAmount());
                ps.setBigDecimal(17, item.getOrderCommissionAmount());
                ps.setBigDecimal(18, item.getOrderNetAmount());
                ps.setBigDecimal(19, item.getFeeSettlementAmount());
                ps.setBigDecimal(20, item.getFeeCommissionAmount());
                ps.setBigDecimal(21, item.getFeeNetAmount());
                ps.setBigDecimal(22, item.getResolvedSettlementAmount());
                ps.setBigDecimal(23, item.getResolvedCommissionAmount());
                ps.setBigDecimal(24, item.getResolvedNetAmount());
                ps.setBoolean(25, item.isSettlementAmountMatched());
                ps.setBoolean(26, item.isCommissionAmountMatched());
                ps.setBoolean(27, item.isNetAmountMatched());
                ps.setInt(28, item.getIssueCount());

                if (item.getLastAggregatedAt() != null) {
                    ps.setTimestamp(29, Timestamp.valueOf(item.getLastAggregatedAt()));
                } else {
                    ps.setNull(29, Types.TIMESTAMP);
                }
            });
        }
    }
}