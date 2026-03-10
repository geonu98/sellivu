package com.sellivu.backend.settlement.parser;

import com.sellivu.backend.settlement.domain.SettlementFileType;
import com.sellivu.backend.settlement.domain.StandardSettlementField;
import java.util.EnumSet;
import java.util.Set;

public final class SettlementRequiredFields {

    private SettlementRequiredFields() {
    }

    public static Set<StandardSettlementField> getRequiredFields(SettlementFileType fileType) {
        return switch (fileType) {
            case DAILY_SETTLEMENT -> EnumSet.of(
                    StandardSettlementField.SETTLEMENT_SCHEDULED_DATE,
                    StandardSettlementField.SETTLEMENT_COMPLETED_DATE,
                    StandardSettlementField.SETTLEMENT_AMOUNT,
                    StandardSettlementField.SETTLEMENT_BASE_AMOUNT,
                    StandardSettlementField.TOTAL_FEE_AMOUNT
            );
            case ORDER_SETTLEMENT -> EnumSet.of(
                    StandardSettlementField.ORDER_NO,
                    StandardSettlementField.PRODUCT_ORDER_NO,
                    StandardSettlementField.SETTLEMENT_BASE_AMOUNT,
                    StandardSettlementField.SETTLEMENT_EXPECTED_AMOUNT
            );
            case FEE_DETAIL -> EnumSet.of(
                    StandardSettlementField.ORDER_NO,
                    StandardSettlementField.PRODUCT_ORDER_NO,
                    StandardSettlementField.FEE_BASE_AMOUNT,
                    StandardSettlementField.FEE_TYPE,
                    StandardSettlementField.COMMISSION_AMOUNT
            );
        };
    }
}