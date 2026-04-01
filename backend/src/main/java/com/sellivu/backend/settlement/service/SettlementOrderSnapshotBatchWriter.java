package com.sellivu.backend.settlement.service;

import com.sellivu.backend.settlement.domain.SettlementOrderSnapshot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.PGConnection;
import org.postgresql.copy.CopyManager;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementOrderSnapshotBatchWriter {

    private final DataSource dataSource;

    public void insertBatch(List<SettlementOrderSnapshot> snapshots) {
        if (snapshots == null || snapshots.isEmpty()) {
            return;
        }

        Long runId = snapshots.get(0).getRunId();
        long totalStart = System.currentTimeMillis();

        String copySql = """
                COPY settlement_order_snapshot (
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
                    has_issue,
                    issue_count,
                    issue_mask,
                    primary_issue_code,
                    refund_candidate,
                    needs_user_input,
                    last_aggregated_at
                )
                FROM STDIN WITH (
                    FORMAT text
                )
                """;

        long buildStart = System.currentTimeMillis();
        StringBuilder sb = new StringBuilder(Math.max(1024, snapshots.size() * 220));

        for (SettlementOrderSnapshot item : snapshots) {
            append(sb, item.getRunId());
            append(sb, item.getJoinKey());
            append(sb, item.getOrderNo());
            append(sb, item.getProductOrderNo());
            append(sb, item.getMatchStatus() != null ? item.getMatchStatus().name() : null);
            append(sb, item.getOrderRowId());
            append(sb, item.getFeeRowId());
            append(sb, item.getOrderUploadId());
            append(sb, item.getFeeUploadId());
            append(sb, item.getProductName());
            append(sb, item.getPaidAt());
            append(sb, item.getSettlementDate());
            append(sb, item.getOrderSettlementAmount());
            append(sb, item.getOrderCommissionAmount());
            append(sb, item.getOrderNetAmount());
            append(sb, item.getFeeSettlementAmount());
            append(sb, item.getFeeCommissionAmount());
            append(sb, item.getFeeNetAmount());
            append(sb, item.getResolvedSettlementAmount());
            append(sb, item.getResolvedCommissionAmount());
            append(sb, item.getResolvedNetAmount());
            append(sb, item.isSettlementAmountMatched());
            append(sb, item.isCommissionAmountMatched());
            append(sb, item.isNetAmountMatched());
            append(sb, item.isHasIssue());
            append(sb, item.getIssueCount());
            append(sb, item.getIssueMask());
            append(sb, item.getPrimaryIssueCode());
            append(sb, item.isRefundCandidate());
            append(sb, item.isNeedsUserInput());
            appendLast(sb, item.getLastAggregatedAt());
        }

        String payload = sb.toString();
        log.info(
                "[PERF] snapshot copy payload build runId={} rows={} chars={} took={}ms",
                runId,
                snapshots.size(),
                payload.length(),
                System.currentTimeMillis() - buildStart
        );

        long encodeStart = System.currentTimeMillis();
        byte[] payloadBytes = payload.getBytes(StandardCharsets.UTF_8);
        log.info(
                "[PERF] snapshot copy encode runId={} rows={} bytes={} took={}ms",
                runId,
                snapshots.size(),
                payloadBytes.length,
                System.currentTimeMillis() - encodeStart
        );

        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (InputStream inputStream = new ByteArrayInputStream(payloadBytes)) {
            PGConnection pgConnection = connection.unwrap(PGConnection.class);
            CopyManager copyManager = pgConnection.getCopyAPI();

            long copyStart = System.currentTimeMillis();
            copyManager.copyIn(copySql, inputStream);
            log.info(
                    "[PERF] snapshot copy copyIn runId={} rows={} took={}ms",
                    runId,
                    snapshots.size(),
                    System.currentTimeMillis() - copyStart
            );
        } catch (Exception e) {
            throw new RuntimeException("settlement_order_snapshot COPY insert ?ㅽ뙣", e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }

        log.info(
                "[PERF] snapshot writer total runId={} rows={} took={}ms",
                runId,
                snapshots.size(),
                System.currentTimeMillis() - totalStart
        );
    }

    private void append(StringBuilder sb, Object value) {
        appendValue(sb, value);
        sb.append('\t');
    }

    private void appendLast(StringBuilder sb, Object value) {
        appendValue(sb, value);
        sb.append('\n');
    }

    private void appendValue(StringBuilder sb, Object value) {
        if (value == null) {
            sb.append("\\N");
            return;
        }
        appendEscapedCopyText(sb, stringify(value));
    }

    private String stringify(Object value) {
        if (value instanceof BigDecimal bigDecimal) return bigDecimal.toPlainString();
        if (value instanceof LocalDate localDate) return localDate.toString();
        if (value instanceof LocalDateTime localDateTime) return localDateTime.toString().replace('T', ' ');
        if (value instanceof Boolean bool) return bool ? "t" : "f";
        return String.valueOf(value);
    }

    private void appendEscapedCopyText(StringBuilder sb, String value) {
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            switch (ch) {
                case '\\' -> sb.append("\\\\");
                case '\t' -> sb.append("\\t");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                default -> sb.append(ch);
            }
        }
    }
}
