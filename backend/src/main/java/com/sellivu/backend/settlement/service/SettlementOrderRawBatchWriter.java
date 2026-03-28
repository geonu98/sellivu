package com.sellivu.backend.settlement.service;

import com.sellivu.backend.settlement.domain.SettlementOrderRow;
import lombok.RequiredArgsConstructor;
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

@Component
@RequiredArgsConstructor
public class SettlementOrderRawBatchWriter {

    private final DataSource dataSource;

    public int write(Long runId, List<SettlementOrderRow> rows) {
        if (rows == null || rows.isEmpty()) {
            return 0;
        }

        String copySql = """
                COPY settlement_order_raw (
                    run_id,
                    upload_id,
                    row_no,
                    join_key,
                    order_no,
                    product_order_no,
                    section_type,
                    product_name,
                    buyer_name,
                    payment_date,
                    amount_changed_date,
                    settlement_scheduled_date,
                    settlement_completed_date,
                    settlement_base_date,
                    tax_report_base_date,
                    settlement_status,
                    settlement_base_amount,
                    npay_fee_amount,
                    sales_linked_fee_total,
                    installment_fee_amount,
                    benefit_amount,
                    settlement_expected_amount,
                    contract_no,
                    created_at
                )
                FROM STDIN WITH (
                    FORMAT text
                )
                """;

        LocalDateTime now = LocalDateTime.now();
        StringBuilder sb = new StringBuilder(Math.max(1024, rows.size() * 220));

        for (SettlementOrderRow row : rows) {
            append(sb, runId);
            append(sb, row.getUploadId());
            append(sb, row.getRowNo());
            append(sb, buildJoinKey(row.getOrderNo(), row.getProductOrderNo()));
            append(sb, row.getOrderNo());
            append(sb, row.getProductOrderNo());
            append(sb, row.getSectionType());
            append(sb, row.getProductName());
            append(sb, row.getBuyerName());
            append(sb, row.getPaymentDate());
            append(sb, row.getAmountChangedDate());
            append(sb, row.getSettlementScheduledDate());
            append(sb, row.getSettlementCompletedDate());
            append(sb, row.getSettlementBaseDate());
            append(sb, row.getTaxReportBaseDate());
            append(sb, row.getSettlementStatus());
            append(sb, row.getSettlementBaseAmount());
            append(sb, row.getNpayFeeAmount());
            append(sb, row.getSalesLinkedFeeTotal());
            append(sb, row.getInstallmentFeeAmount());
            append(sb, row.getBenefitAmount());
            append(sb, row.getSettlementExpectedAmount());
            append(sb, row.getContractNo());
            appendLast(sb, now);
        }

        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (InputStream inputStream = new ByteArrayInputStream(sb.toString().getBytes(StandardCharsets.UTF_8))) {
            PGConnection pgConnection = connection.unwrap(PGConnection.class);
            CopyManager copyManager = pgConnection.getCopyAPI();
            copyManager.copyIn(copySql, inputStream);
            return rows.size();
        } catch (Exception e) {
            throw new RuntimeException("settlement_order_raw COPY insert 실패", e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    private String buildJoinKey(String orderNo, String productOrderNo) {
        if (productOrderNo != null && !productOrderNo.isBlank()) {
            return "P:" + productOrderNo.trim();
        }
        return "O:" + (orderNo == null ? "" : orderNo.trim());
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
        sb.append(escapeCopyText(stringify(value)));
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

    private String escapeCopyText(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\t", "\\t")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}