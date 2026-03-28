package com.sellivu.backend.settlement.service;

import com.sellivu.backend.settlement.domain.SettlementOrderRow;
import com.sellivu.backend.settlement.domain.StandardSettlementField;
import com.sellivu.backend.settlement.parser.SettlementParsedRow;
import com.sellivu.backend.settlement.parser.SettlementValueParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SettlementOrderRowMapper {

    private final SettlementValueParser valueParser;

    public SettlementOrderRow map(Long uploadId, SettlementParsedRow row) {
        String orderNo = row.get(StandardSettlementField.ORDER_NO);
        String productOrderNo = row.get(StandardSettlementField.PRODUCT_ORDER_NO);
        String sectionType = row.get(StandardSettlementField.SECTION_TYPE);
        String productName = row.get(StandardSettlementField.PRODUCT_NAME);
        String buyerName = row.get(StandardSettlementField.BUYER_NAME);
        String paymentDate = row.get(StandardSettlementField.PAYMENT_DATE);
        String amountChangedDate = row.get(StandardSettlementField.AMOUNT_CHANGED_DATE);
        String settlementScheduledDate = row.get(StandardSettlementField.SETTLEMENT_SCHEDULED_DATE);
        String settlementCompletedDate = row.get(StandardSettlementField.SETTLEMENT_COMPLETED_DATE);
        String settlementBaseDate = row.get(StandardSettlementField.SETTLEMENT_BASE_DATE);
        String taxReportBaseDate = row.get(StandardSettlementField.TAX_REPORT_BASE_DATE);
        String settlementStatus = row.get(StandardSettlementField.SETTLEMENT_STATUS);
        String settlementBaseAmount = row.get(StandardSettlementField.SETTLEMENT_BASE_AMOUNT);
        String npayFeeAmount = row.get(StandardSettlementField.NPAY_FEE_AMOUNT);
        String salesLinkedFeeTotal = row.get(StandardSettlementField.SALES_LINKED_FEE_TOTAL);
        String installmentFeeAmount = row.get(StandardSettlementField.INSTALLMENT_FEE_AMOUNT);
        String benefitAmount = row.get(StandardSettlementField.BENEFIT_AMOUNT);
        String settlementExpectedAmount = row.get(StandardSettlementField.SETTLEMENT_EXPECTED_AMOUNT);
        String contractNo = row.get(StandardSettlementField.CONTRACT_NO);

        return new SettlementOrderRow(
                uploadId,
                row.getRowNumber(),
                valueParser.asString(orderNo),
                valueParser.asString(productOrderNo),
                valueParser.asString(sectionType),
                valueParser.asString(productName),
                valueParser.asString(buyerName),
                valueParser.asLocalDate(paymentDate),
                valueParser.asLocalDate(amountChangedDate),
                valueParser.asLocalDate(settlementScheduledDate),
                valueParser.asLocalDate(settlementCompletedDate),
                valueParser.asLocalDate(settlementBaseDate),
                valueParser.asLocalDate(taxReportBaseDate),
                valueParser.asString(settlementStatus),
                valueParser.asBigDecimal(settlementBaseAmount),
                valueParser.asBigDecimal(npayFeeAmount),
                valueParser.asBigDecimal(salesLinkedFeeTotal),
                valueParser.asBigDecimal(installmentFeeAmount),
                valueParser.asBigDecimal(benefitAmount),
                valueParser.asBigDecimal(settlementExpectedAmount),
                valueParser.asString(contractNo)
        );
    }
}