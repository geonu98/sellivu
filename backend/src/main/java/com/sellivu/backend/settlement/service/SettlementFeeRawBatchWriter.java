package com.sellivu.backend.settlement.service;

import com.sellivu.backend.settlement.domain.SettlementFeeRow;
import com.sellivu.backend.settlement.domain.StandardSettlementField;
import com.sellivu.backend.settlement.parser.SettlementParsedRow;
import com.sellivu.backend.settlement.parser.SettlementValueParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.PGConnection;
import org.postgresql.copy.CopyManager;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
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
public class SettlementFeeRawBatchWriter {

    private final DataSource dataSource;
    private final SettlementValueParser valueParser;

    public int write(Long runId, List<SettlementFeeRow> rows) {
        if (rows == null || rows.isEmpty()) {
            return 0;
        }
        long totalStart = System.currentTimeMillis();

        String copySql = """
                COPY settlement_fee_raw (
                    run_id,
                    upload_id,
                    row_no,
                    join_key,
                    order_no,
                    product_order_no,
                    section_type,
                    product_name,
                    buyer_name,
                    settlement_scheduled_date,
                    settlement_completed_date,
                    settlement_base_date,
                    tax_report_base_date,
                    settlement_status,
                    fee_base_amount,
                    fee_type,
                    payment_method,
                    sales_linked_fee_detail,
                    fee_cap_amount,
                    commission_amount,
                    created_at
                )
                FROM STDIN WITH (
                    FORMAT text
                )
                """;

        LocalDateTime now = LocalDateTime.now();
        String createdAtText = stringify(now);

        log.info(
                "[PERF] rawFee copy payload build runId={} rows={} chars={} took={}ms source=stream",
                runId,
                rows.size(),
                -1,
                0
        );
        log.info(
                "[PERF] rawFee copy encode runId={} rows={} bytes={} took={}ms source=stream",
                runId,
                rows.size(),
                -1,
                0
        );

        Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            PGConnection pgConnection = connection.unwrap(PGConnection.class);
            CopyManager copyManager = pgConnection.getCopyAPI();
            long copyInStart = System.currentTimeMillis();
            CopyStreamSupport.copyIn(copyManager, copySql, writer -> writeRows(writer, runId, rows, createdAtText));
            log.info(
                    "[PERF] rawFee copy copyIn runId={} rows={} took={}ms",
                    runId,
                    rows.size(),
                    System.currentTimeMillis() - copyInStart
            );
            log.info(
                    "[PERF] rawFee writer total runId={} rows={} took={}ms",
                    runId,
                    rows.size(),
                    System.currentTimeMillis() - totalStart
            );
            return rows.size();
        } catch (Exception e) {
            throw new RuntimeException("settlement_fee_raw COPY insert failed", e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    public int writeParsed(Long runId, Long uploadId, List<SettlementParsedRow> rows) {
        if (rows == null || rows.isEmpty()) {
            return 0;
        }

        long totalStart = System.currentTimeMillis();
        String copySql = """
                COPY settlement_fee_raw (
                    run_id,
                    upload_id,
                    row_no,
                    join_key,
                    order_no,
                    product_order_no,
                    section_type,
                    product_name,
                    buyer_name,
                    settlement_scheduled_date,
                    settlement_completed_date,
                    settlement_base_date,
                    tax_report_base_date,
                    settlement_status,
                    fee_base_amount,
                    fee_type,
                    payment_method,
                    sales_linked_fee_detail,
                    fee_cap_amount,
                    commission_amount,
                    created_at
                )
                FROM STDIN WITH (
                    FORMAT text
                )
                """;

        LocalDateTime now = LocalDateTime.now();
        String createdAtText = stringify(now);

        log.info(
                "[PERF] rawFee copy payload build runId={} rows={} chars={} took={}ms source=stream",
                runId,
                rows.size(),
                -1,
                0
        );
        log.info(
                "[PERF] rawFee copy encode runId={} rows={} bytes={} took={}ms source=stream",
                runId,
                rows.size(),
                -1,
                0
        );

        Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            PGConnection pgConnection = connection.unwrap(PGConnection.class);
            CopyManager copyManager = pgConnection.getCopyAPI();
            long copyInStart = System.currentTimeMillis();
            CopyStreamSupport.copyIn(copyManager, copySql, writer -> writeParsedRows(writer, runId, uploadId, rows, createdAtText));
            log.info(
                    "[PERF] rawFee copy copyIn runId={} rows={} took={}ms",
                    runId,
                    rows.size(),
                    System.currentTimeMillis() - copyInStart
            );
            log.info(
                    "[PERF] rawFee writer total runId={} rows={} took={}ms",
                    runId,
                    rows.size(),
                    System.currentTimeMillis() - totalStart
            );
            return rows.size();
        } catch (Exception e) {
            throw new RuntimeException("settlement_fee_raw COPY insert failed", e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    private void writeRows(BufferedWriter writer, Long runId, List<SettlementFeeRow> rows, String createdAtText) throws IOException {
        for (SettlementFeeRow row : rows) {
            append(writer, runId);
            append(writer, row.getUploadId());
            append(writer, row.getRowNo());
            append(writer, buildJoinKey(row.getOrderNo(), row.getProductOrderNo()));
            append(writer, row.getOrderNo());
            append(writer, row.getProductOrderNo());
            append(writer, row.getSectionType());
            append(writer, row.getProductName());
            append(writer, row.getBuyerName());
            append(writer, row.getSettlementScheduledDate());
            append(writer, row.getSettlementCompletedDate());
            append(writer, row.getSettlementBaseDate());
            append(writer, row.getTaxReportBaseDate());
            append(writer, row.getSettlementStatus());
            append(writer, row.getFeeBaseAmount());
            append(writer, row.getFeeType());
            append(writer, row.getPaymentMethod());
            append(writer, row.getSalesLinkedFeeDetail());
            append(writer, row.getFeeCapAmount());
            append(writer, row.getCommissionAmount());
            appendLastText(writer, createdAtText);
        }
    }

    private void writeParsedRows(BufferedWriter writer, Long runId, Long uploadId, List<SettlementParsedRow> rows, String createdAtText) throws IOException {
        for (SettlementParsedRow row : rows) {
            String orderNo = valueParser.asString(row.get(StandardSettlementField.ORDER_NO));
            String productOrderNo = valueParser.asString(row.get(StandardSettlementField.PRODUCT_ORDER_NO));

            append(writer, runId);
            append(writer, uploadId);
            append(writer, row.getRowNumber());
            append(writer, buildJoinKey(orderNo, productOrderNo));
            append(writer, orderNo);
            append(writer, productOrderNo);
            append(writer, valueParser.asString(row.get(StandardSettlementField.SECTION_TYPE)));
            append(writer, valueParser.asString(row.get(StandardSettlementField.PRODUCT_NAME)));
            append(writer, valueParser.asString(row.get(StandardSettlementField.BUYER_NAME)));
            append(writer, valueParser.asLocalDate(row.get(StandardSettlementField.SETTLEMENT_SCHEDULED_DATE)));
            append(writer, valueParser.asLocalDate(row.get(StandardSettlementField.SETTLEMENT_COMPLETED_DATE)));
            append(writer, valueParser.asLocalDate(row.get(StandardSettlementField.SETTLEMENT_BASE_DATE)));
            append(writer, valueParser.asLocalDate(row.get(StandardSettlementField.TAX_REPORT_BASE_DATE)));
            append(writer, valueParser.asString(row.get(StandardSettlementField.SETTLEMENT_STATUS)));
            append(writer, valueParser.asBigDecimal(row.get(StandardSettlementField.FEE_BASE_AMOUNT)));
            append(writer, valueParser.asString(row.get(StandardSettlementField.FEE_TYPE)));
            append(writer, valueParser.asString(row.get(StandardSettlementField.PAYMENT_METHOD)));
            append(writer, valueParser.asString(row.get(StandardSettlementField.SALES_LINKED_FEE_DETAIL)));
            append(writer, valueParser.asBigDecimal(row.get(StandardSettlementField.FEE_CAP_AMOUNT)));
            append(writer, valueParser.asBigDecimal(row.get(StandardSettlementField.COMMISSION_AMOUNT)));
            appendLastText(writer, createdAtText);
        }
    }

    private String buildJoinKey(String orderNo, String productOrderNo) {
        if (productOrderNo != null && !productOrderNo.isBlank()) {
            return "P:" + productOrderNo.trim();
        }
        return "O:" + (orderNo == null ? "" : orderNo.trim());
    }

    private void append(BufferedWriter writer, Object value) throws IOException {
        appendValue(writer, value);
        writer.write('\t');
    }

    private void appendLastText(BufferedWriter writer, String value) throws IOException {
        appendEscapedCopyText(writer, value);
        writer.write('\n');
    }

    private void appendValue(BufferedWriter writer, Object value) throws IOException {
        if (value == null) {
            writer.write("\\N");
            return;
        }
        appendEscapedCopyText(writer, stringify(value));
    }

    private String stringify(Object value) {
        if (value instanceof BigDecimal bigDecimal) {
            return bigDecimal.toPlainString();
        }
        if (value instanceof LocalDate localDate) {
            return localDate.toString();
        }
        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime.toString().replace('T', ' ');
        }
        if (value instanceof Boolean bool) {
            return bool ? "t" : "f";
        }
        return String.valueOf(value);
    }

    private void appendEscapedCopyText(BufferedWriter writer, String value) throws IOException {
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            switch (ch) {
                case '\\' -> writer.write("\\\\");
                case '\t' -> writer.write("\\t");
                case '\n' -> writer.write("\\n");
                case '\r' -> writer.write("\\r");
                default -> writer.write(ch);
            }
        }
    }
}
