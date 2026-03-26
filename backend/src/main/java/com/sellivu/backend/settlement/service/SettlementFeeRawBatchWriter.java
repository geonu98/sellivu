package com.sellivu.backend.settlement.service;

import com.sellivu.backend.settlement.domain.SettlementFeeRow;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SettlementFeeRawBatchWriter {

    private final JdbcTemplate jdbcTemplate;

    public int write(Long runId, List<SettlementFeeRow> rows) {
        if (rows == null || rows.isEmpty()) {
            return 0;
        }

        String sql = """
                INSERT INTO settlement_fee_raw (
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
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        LocalDateTime now = LocalDateTime.now();

        int[] result = jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                SettlementFeeRow row = rows.get(i);

                ps.setLong(1, runId);
                setLong(ps, 2, row.getUploadId());
                setInteger(ps, 3, row.getRowNo());
                ps.setString(4, buildJoinKey(row.getOrderNo(), row.getProductOrderNo()));
                ps.setString(5, row.getOrderNo());
                ps.setString(6, row.getProductOrderNo());
                ps.setString(7, row.getSectionType());
                ps.setString(8, row.getProductName());
                ps.setString(9, row.getBuyerName());
                setLocalDate(ps, 10, row.getSettlementScheduledDate());
                setLocalDate(ps, 11, row.getSettlementCompletedDate());
                setLocalDate(ps, 12, row.getSettlementBaseDate());
                setLocalDate(ps, 13, row.getTaxReportBaseDate());
                ps.setString(14, row.getSettlementStatus());
                setBigDecimal(ps, 15, row.getFeeBaseAmount());
                ps.setString(16, row.getFeeType());
                ps.setString(17, row.getPaymentMethod());
                ps.setString(18, row.getSalesLinkedFeeDetail());
                setBigDecimal(ps, 19, row.getFeeCapAmount());
                setBigDecimal(ps, 20, row.getCommissionAmount());
                ps.setTimestamp(21, Timestamp.valueOf(now));
            }

            @Override
            public int getBatchSize() {
                return rows.size();
            }
        });

        return result.length;
    }

    private String buildJoinKey(String orderNo, String productOrderNo) {
        if (productOrderNo != null && !productOrderNo.isBlank()) {
            return "P:" + productOrderNo.trim();
        }
        return "O:" + (orderNo == null ? "" : orderNo.trim());
    }

    private void setLong(PreparedStatement ps, int index, Long value) throws SQLException {
        if (value == null) {
            ps.setNull(index, java.sql.Types.BIGINT);
        } else {
            ps.setLong(index, value);
        }
    }

    private void setInteger(PreparedStatement ps, int index, Integer value) throws SQLException {
        if (value == null) {
            ps.setNull(index, java.sql.Types.INTEGER);
        } else {
            ps.setInt(index, value);
        }
    }

    private void setLocalDate(PreparedStatement ps, int index, java.time.LocalDate value) throws SQLException {
        if (value == null) {
            ps.setNull(index, java.sql.Types.DATE);
        } else {
            ps.setDate(index, Date.valueOf(value));
        }
    }

    private void setBigDecimal(PreparedStatement ps, int index, BigDecimal value) throws SQLException {
        if (value == null) {
            ps.setNull(index, java.sql.Types.NUMERIC);
        } else {
            ps.setBigDecimal(index, value);
        }
    }
}