package com.sellivu.backend.settlement.parser;

import com.sellivu.backend.settlement.domain.SettlementFileType;
import com.sellivu.backend.settlement.domain.StandardSettlementField;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SettlementHeaderValidatorTest {

    private final SettlementHeaderValidator validator = new SettlementHeaderValidator();

    @Test
    @DisplayName("ORDER_SETTLEMENT 필수 헤더가 모두 있으면 통과한다")
    void validateRequiredHeaders_shouldPassWhenAllRequiredOrderHeadersExist() {
        Map<String, StandardSettlementField> mappedHeaders = Map.of(
                "주문번호", StandardSettlementField.ORDER_NO,
                "상품주문번호", StandardSettlementField.PRODUCT_ORDER_NO,
                "결제일", StandardSettlementField.PAYMENT_DATE,
                "금액변동일", StandardSettlementField.AMOUNT_CHANGED_DATE,
                "정산기준금액", StandardSettlementField.SETTLEMENT_BASE_AMOUNT,
                "npay수수료", StandardSettlementField.NPAY_FEE_AMOUNT,
                "매출연동수수료합계", StandardSettlementField.SALES_LINKED_FEE_TOTAL,
                "정산예정금액", StandardSettlementField.SETTLEMENT_EXPECTED_AMOUNT
        );

        assertDoesNotThrow(() ->
                validator.validateRequiredHeaders(SettlementFileType.ORDER_SETTLEMENT, mappedHeaders)
        );
    }

    @Test
    @DisplayName("FEE_DETAIL 필수 헤더가 모두 있으면 통과한다")
    void validateRequiredHeaders_shouldPassWhenAllRequiredFeeHeadersExist() {
        Map<String, StandardSettlementField> mappedHeaders = Map.of(
                "주문번호", StandardSettlementField.ORDER_NO,
                "상품주문번호", StandardSettlementField.PRODUCT_ORDER_NO,
                "수수료기준금액", StandardSettlementField.FEE_BASE_AMOUNT,
                "수수료구분", StandardSettlementField.FEE_TYPE,
                "결제수단", StandardSettlementField.PAYMENT_METHOD,
                "매출연동수수료상세", StandardSettlementField.SALES_LINKED_FEE_DETAIL,
                "수수료상한액", StandardSettlementField.FEE_CAP_AMOUNT,
                "수수료금액", StandardSettlementField.COMMISSION_AMOUNT
        );

        assertDoesNotThrow(() ->
                validator.validateRequiredHeaders(SettlementFileType.FEE_DETAIL, mappedHeaders)
        );
    }

    @Test
    @DisplayName("필수 헤더가 누락되면 예외를 던지고 누락 헤더명을 포함한다")
    void validateRequiredHeaders_shouldThrowWhenRequiredHeadersMissing() {
        Map<String, StandardSettlementField> mappedHeaders = Map.of(
                "주문번호", StandardSettlementField.ORDER_NO,
                "상품주문번호", StandardSettlementField.PRODUCT_ORDER_NO
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validateRequiredHeaders(SettlementFileType.ORDER_SETTLEMENT, mappedHeaders)
        );

        assertTrue(exception.getMessage().contains("필수 헤더가 누락되었습니다."));
        assertTrue(exception.getMessage().contains("fileType=ORDER_SETTLEMENT"));
    }

    @Test
    @DisplayName("같은 StandardSettlementField가 하나라도 매핑되어 있으면 필수 헤더가 존재하는 것으로 본다")
    void validateRequiredHeaders_shouldCheckMappedFieldValues() {
        Map<String, StandardSettlementField> mappedHeaders = Map.of(
                "원본_주문번호", StandardSettlementField.ORDER_NO,
                "원본_상품주문번호", StandardSettlementField.PRODUCT_ORDER_NO,
                "원본_결제일", StandardSettlementField.PAYMENT_DATE,
                "원본_금액변동일", StandardSettlementField.AMOUNT_CHANGED_DATE,
                "원본_정산기준금액", StandardSettlementField.SETTLEMENT_BASE_AMOUNT,
                "원본_npay수수료", StandardSettlementField.NPAY_FEE_AMOUNT,
                "원본_매출연동수수료합계", StandardSettlementField.SALES_LINKED_FEE_TOTAL,
                "원본_정산예정금액", StandardSettlementField.SETTLEMENT_EXPECTED_AMOUNT
        );

        assertDoesNotThrow(() ->
                validator.validateRequiredHeaders(SettlementFileType.ORDER_SETTLEMENT, mappedHeaders)
        );
    }
}