package com.sellivu.backend.settlement.service;

import com.sellivu.backend.settlement.domain.SettlementFeeRow;
import com.sellivu.backend.settlement.domain.StandardSettlementField;
import com.sellivu.backend.settlement.parser.SettlementParsedRow;
import com.sellivu.backend.settlement.parser.SettlementValueParser;
import org.springframework.stereotype.Component;

@Component
public class SettlementFeeRowMapper {

    private final SettlementValueParser valueParser = new SettlementValueParser();

    public SettlementFeeRow map(Long uploadId, SettlementParsedRow row) {
        return new SettlementFeeRow(
                uploadId,
                row.getRowNumber(),
                valueParser.asString(row.get(StandardSettlementField.ORDER_NO)),
                valueParser.asString(row.get(StandardSettlementField.PRODUCT_ORDER_NO)),
                valueParser.asString(row.get(StandardSettlementField.SECTION_TYPE)),
                valueParser.asString(row.get(StandardSettlementField.PRODUCT_NAME)),
                valueParser.asString(row.get(StandardSettlementField.BUYER_NAME)),
                valueParser.asLocalDate(row.get(StandardSettlementField.SETTLEMENT_SCHEDULED_DATE)),
                valueParser.asLocalDate(row.get(StandardSettlementField.SETTLEMENT_COMPLETED_DATE)),
                valueParser.asLocalDate(row.get(StandardSettlementField.SETTLEMENT_BASE_DATE)),
                valueParser.asLocalDate(row.get(StandardSettlementField.TAX_REPORT_BASE_DATE)),
                valueParser.asString(row.get(StandardSettlementField.SETTLEMENT_STATUS)),
                valueParser.asBigDecimal(row.get(StandardSettlementField.FEE_BASE_AMOUNT)),
                valueParser.asString(row.get(StandardSettlementField.FEE_TYPE)),
                valueParser.asString(row.get(StandardSettlementField.PAYMENT_METHOD)),
                valueParser.asString(row.get(StandardSettlementField.SALES_LINKED_FEE_DETAIL)),
                valueParser.asBigDecimal(row.get(StandardSettlementField.FEE_CAP_AMOUNT)),
                valueParser.asBigDecimal(row.get(StandardSettlementField.COMMISSION_AMOUNT))
        );
    }
}