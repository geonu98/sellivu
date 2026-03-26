package com.sellivu.backend.settlement.service;

import com.sellivu.backend.settlement.domain.SettlementDailyRow;
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
public class SettlementDailyRawBatchWriter {

    private final JdbcTemplate jdbcTemplate;

    public int write(Long runId, List<SettlementDailyRow> rows) {
        if (rows == null || rows.isEmpty()) {
            return 0;
        }

        String sql = """
                INSERT INTO settlement_daily_raw (
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
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        LocalDateTime now = LocalDateTime.now();

        int[] result = jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                SettlementDailyRow row = rows.get(i);

                ps.setLong(1, runId);
                setLong(ps, 2, row.getUploadId());
                setInteger(ps, 3, row.getRowNo());
                setLocalDate(ps, 4, row.getSettlementScheduledDate());
                setLocalDate(ps, 5, row.getSettlementCompletedDate());
                setBigDecimal(ps, 6, row.getSettlementAmount());
                setBigDecimal(ps, 7, row.getGeneralSettlementAmount());
                setBigDecimal(ps, 8, row.getFastSettlementAmount());
                setBigDecimal(ps, 9, row.getSettlementBaseAmount());
                setBigDecimal(ps, 10, row.getTotalFeeAmount());
                setBigDecimal(ps, 11, row.getBenefitSettlementAmount());
                setBigDecimal(ps, 12, row.getDailyDeductionRefundAmount());
                setBigDecimal(ps, 13, row.getHoldAmount());
                setBigDecimal(ps, 14, row.getBizWalletOffsetAmount());
                setBigDecimal(ps, 15, row.getSafeReturnCareCost());
                setBigDecimal(ps, 16, row.getPreferredFeeRefundAmount());
                ps.setString(17, row.getSettlementMethod());
                ps.setTimestamp(18, Timestamp.valueOf(now));
            }

            @Override
            public int getBatchSize() {
                return rows.size();
            }
        });

        return result.length;
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