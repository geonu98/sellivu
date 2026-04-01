package com.sellivu.backend.settlement.service;

import com.sellivu.backend.settlement.domain.SettlementDailyRow;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.PGConnection;
import org.postgresql.copy.CopyManager;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementDailyRawBatchWriter {

    private final JdbcTemplate jdbcTemplate;

    public int write(Long runId, List<SettlementDailyRow> rows) {
        if (rows == null || rows.isEmpty()) {
            return 0;
        }
        long totalStart = System.currentTimeMillis();

        String copySql = """
                COPY settlement_daily_raw (
                    run_id,
                    upload_id,
                    row_no,
                    settlement_scheduled_date,
                    settlement_completed_date,
                    settlement_amount,
                    general_settlement_amount,
                    fast_settlement_amount,
                    settlement_base_amount,
                    total_fee_amount,
                    benefit_settlement_amount,
                    daily_deduction_refund_amount,
                    hold_amount,
                    biz_wallet_offset_amount,
                    safe_return_care_cost,
                    preferred_fee_refund_amount,
                    settlement_method,
                    created_at
                )
                FROM STDIN WITH (FORMAT csv)
                """;

        LocalDateTime now = LocalDateTime.now();
        String createdAtText = toText(now);

        log.info(
                "[PERF] rawDaily copy payload build runId={} rows={} chars={} took={}ms source=stream",
                runId,
                rows.size(),
                -1,
                0
        );
        log.info(
                "[PERF] rawDaily copy encode runId={} rows={} bytes={} took={}ms source=stream",
                runId,
                rows.size(),
                -1,
                0
        );

        return jdbcTemplate.execute((Connection connection) -> {
            try {
                PGConnection pgConnection = connection.unwrap(PGConnection.class);
                CopyManager copyManager = pgConnection.getCopyAPI();
                long copyInStart = System.currentTimeMillis();
                CopyStreamSupport.copyIn(copyManager, copySql, writer -> writeRows(writer, runId, rows, createdAtText));
                log.info(
                        "[PERF] rawDaily copy copyIn runId={} rows={} took={}ms",
                        runId,
                        rows.size(),
                        System.currentTimeMillis() - copyInStart
                );
                log.info(
                        "[PERF] rawDaily writer total runId={} rows={} took={}ms",
                        runId,
                        rows.size(),
                        System.currentTimeMillis() - totalStart
                );
                return rows.size();
            } catch (Exception e) {
                throw new RuntimeException("settlement_daily_raw COPY insert failed", e);
            }
        });
    }

    private void writeRows(BufferedWriter writer, Long runId, List<SettlementDailyRow> rows, String createdAtText) throws IOException {
        for (SettlementDailyRow row : rows) {
            appendCsv(writer, runId);
            writer.write(',');
            appendCsv(writer, row.getUploadId());
            writer.write(',');
            appendCsv(writer, row.getRowNo());
            writer.write(',');
            appendCsv(writer, row.getSettlementScheduledDate());
            writer.write(',');
            appendCsv(writer, row.getSettlementCompletedDate());
            writer.write(',');
            appendCsv(writer, row.getSettlementAmount());
            writer.write(',');
            appendCsv(writer, row.getGeneralSettlementAmount());
            writer.write(',');
            appendCsv(writer, row.getFastSettlementAmount());
            writer.write(',');
            appendCsv(writer, row.getSettlementBaseAmount());
            writer.write(',');
            appendCsv(writer, row.getTotalFeeAmount());
            writer.write(',');
            appendCsv(writer, row.getBenefitSettlementAmount());
            writer.write(',');
            appendCsv(writer, row.getDailyDeductionRefundAmount());
            writer.write(',');
            appendCsv(writer, row.getHoldAmount());
            writer.write(',');
            appendCsv(writer, row.getBizWalletOffsetAmount());
            writer.write(',');
            appendCsv(writer, row.getSafeReturnCareCost());
            writer.write(',');
            appendCsv(writer, row.getPreferredFeeRefundAmount());
            writer.write(',');
            appendCsv(writer, row.getSettlementMethod());
            writer.write(',');
            appendCsvText(writer, createdAtText);
            writer.write('\n');
        }
    }

    private void appendCsv(BufferedWriter writer, Object value) throws IOException {
        if (value == null) {
            return;
        }

        appendCsvText(writer, toText(value));
    }

    private void appendCsvText(BufferedWriter writer, String text) throws IOException {
        boolean needsQuotes =
                text.indexOf(',') >= 0 ||
                        text.indexOf('"') >= 0 ||
                        text.indexOf('\n') >= 0 ||
                        text.indexOf('\r') >= 0;

        if (!needsQuotes) {
            writer.write(text);
            return;
        }

        writer.write('"');
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch == '"') {
                writer.write("\"\"");
            } else {
                writer.write(ch);
            }
        }
        writer.write('"');
    }

    private String toText(Object value) {
        if (value instanceof BigDecimal decimal) {
            return decimal.toPlainString();
        }
        if (value instanceof LocalDate localDate) {
            return localDate.toString();
        }
        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime.toString().replace('T', ' ');
        }
        return String.valueOf(value);
    }
}
