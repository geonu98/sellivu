package com.sellivu.backend.settlement.service;

import com.sellivu.backend.settlement.domain.SettlementDailyRow;
import com.sellivu.backend.settlement.domain.StandardSettlementField;
import com.sellivu.backend.settlement.parser.SettlementParsedRow;
import com.sellivu.backend.settlement.parser.SettlementValueParser;
import org.springframework.stereotype.Component;

@Component
public class SettlementDailyRowMapper {

    private final SettlementValueParser valueParser = new SettlementValueParser();

    public SettlementDailyRow map(Long uploadId, SettlementParsedRow row) {
        return new SettlementDailyRow(
                uploadId,
                row.getRowNumber(),
                valueParser.asLocalDate(row.get(StandardSettlementField.SETTLEMENT_SCHEDULED_DATE)),
                valueParser.asLocalDate(row.get(StandardSettlementField.SETTLEMENT_COMPLETED_DATE)),
                valueParser.asBigDecimal(row.get(StandardSettlementField.SETTLEMENT_AMOUNT)),
                valueParser.asBigDecimal(row.get(StandardSettlementField.GENERAL_SETTLEMENT_AMOUNT)),
                valueParser.asBigDecimal(row.get(StandardSettlementField.FAST_SETTLEMENT_AMOUNT)),
                valueParser.asBigDecimal(row.get(StandardSettlementField.SETTLEMENT_BASE_AMOUNT)),
                valueParser.asBigDecimal(row.get(StandardSettlementField.TOTAL_FEE_AMOUNT)),
                valueParser.asBigDecimal(row.get(StandardSettlementField.BENEFIT_SETTLEMENT_AMOUNT)),
                valueParser.asBigDecimal(row.get(StandardSettlementField.DAILY_DEDUCTION_REFUND_AMOUNT)),
                valueParser.asBigDecimal(row.get(StandardSettlementField.HOLD_AMOUNT)),
                valueParser.asBigDecimal(row.get(StandardSettlementField.BIZ_WALLET_OFFSET_AMOUNT)),
                valueParser.asBigDecimal(row.get(StandardSettlementField.SAFE_RETURN_CARE_COST)),
                valueParser.asBigDecimal(row.get(StandardSettlementField.PREFERRED_FEE_REFUND_AMOUNT)),
                valueParser.asString(row.get(StandardSettlementField.SETTLEMENT_METHOD))
        );
    }
}