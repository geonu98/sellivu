package com.sellivu.backend.settlement.service;

import com.sellivu.backend.settlement.domain.SettlementOrderRow;
import com.sellivu.backend.settlement.domain.StandardSettlementField;
import com.sellivu.backend.settlement.parser.SettlementParsedRow;
import com.sellivu.backend.settlement.parser.SettlementValueParser;
import org.springframework.stereotype.Component;

@Component
public class SettlementOrderRowMapper {

    private final SettlementValueParser valueParser = new SettlementValueParser();

    public SettlementOrderRow map(Long uploadId, SettlementParsedRow row) {
        return new SettlementOrderRow(
                uploadId,
                row.getRowNumber(),
                valueParser.asString(row.get(StandardSettlementField.ORDER_NO)),
                valueParser.asString(row.get(StandardSettlementField.PRODUCT_ORDER_NO)),
                valueParser.asString(row.get(StandardSettlementField.SECTION_TYPE)),
                valueParser.asString(row.get(StandardSettlementField.PRODUCT_NAME)),
                valueParser.asString(row.get(StandardSettlementField.BUYER_NAME)),
                valueParser.asLocalDate(row.get(StandardSettlementField.PAYMENT_DATE)),
                valueParser.asLocalDate(row.get(StandardSettlementField.AMOUNT_CHANGED_DATE)),
                valueParser.asLocalDate(row.get(StandardSettlementField.SETTLEMENT_SCHEDULED_DATE)),
                valueParser.asLocalDate(row.get(StandardSettlementField.SETTLEMENT_COMPLETED_DATE)),
                valueParser.asLocalDate(row.get(StandardSettlementField.SETTLEMENT_BASE_DATE)),
                valueParser.asLocalDate(row.get(StandardSettlementField.TAX_REPORT_BASE_DATE)),
                valueParser.asString(row.get(StandardSettlementField.SETTLEMENT_STATUS)),
                valueParser.asBigDecimal(row.get(StandardSettlementField.SETTLEMENT_BASE_AMOUNT)),
                valueParser.asBigDecimal(row.get(StandardSettlementField.NPAY_FEE_AMOUNT)),
                valueParser.asBigDecimal(row.get(StandardSettlementField.SALES_LINKED_FEE_TOTAL)),
                valueParser.asBigDecimal(row.get(StandardSettlementField.INSTALLMENT_FEE_AMOUNT)),
                valueParser.asBigDecimal(row.get(StandardSettlementField.BENEFIT_AMOUNT)),
                valueParser.asBigDecimal(row.get(StandardSettlementField.SETTLEMENT_EXPECTED_AMOUNT)),
                valueParser.asString(row.get(StandardSettlementField.CONTRACT_NO))
        );
    }
}