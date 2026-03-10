package com.sellivu.backend.settlement.domain;

import java.util.Arrays;
import java.util.List;

public enum StandardSettlementField {

    ROW_NO("행번호", FieldType.INTEGER,
            "No.", "No", "번호"),

    ORDER_NO("주문번호", FieldType.STRING,
            "주문번호"),

    PRODUCT_ORDER_NO("상품주문번호", FieldType.STRING,
            "상품주문번호", "상품 주문번호"),

    SECTION_TYPE("구분", FieldType.STRING,
            "구분"),

    PRODUCT_NAME("상품명", FieldType.STRING,
            "상품명"),

    BUYER_NAME("구매자명", FieldType.STRING,
            "구매자명"),

    PAYMENT_DATE("결제일", FieldType.DATE,
            "결제일"),

    AMOUNT_CHANGED_DATE("금액 변동일", FieldType.DATE,
            "금액 변동일"),

    SETTLEMENT_SCHEDULED_DATE("정산예정일", FieldType.DATE,
            "정산예정일"),

    SETTLEMENT_COMPLETED_DATE("정산완료일", FieldType.DATE,
            "정산완료일"),

    SETTLEMENT_BASE_DATE("정산기준일", FieldType.DATE,
            "정산기준일"),

    TAX_REPORT_BASE_DATE("세금신고기준일", FieldType.DATE,
            "세금신고기준일"),

    SETTLEMENT_STATUS("정산상태", FieldType.STRING,
            "정산상태"),

    CONTRACT_NO("계약번호", FieldType.STRING,
            "계약번호"),

    SETTLEMENT_BASE_AMOUNT("정산기준금액", FieldType.DECIMAL,
            "정산기준금액"),

    SETTLEMENT_AMOUNT("정산금액", FieldType.DECIMAL,
            "정산금액"),

    GENERAL_SETTLEMENT_AMOUNT("일반정산금액", FieldType.DECIMAL,
            "일반정산금액"),

    FAST_SETTLEMENT_AMOUNT("빠른정산금액", FieldType.DECIMAL,
            "빠른정산금액"),

    TOTAL_FEE_AMOUNT("수수료합계", FieldType.DECIMAL,
            "수수료합계"),

    BENEFIT_SETTLEMENT_AMOUNT("혜택정산", FieldType.DECIMAL,
            "혜택정산"),

    DAILY_DEDUCTION_REFUND_AMOUNT("일별 공제/환급", FieldType.DECIMAL,
            "일별 공제/환급"),

    HOLD_AMOUNT("지급보류", FieldType.DECIMAL,
            "지급보류"),

    BIZ_WALLET_OFFSET_AMOUNT("마이너스비즈월렛상계", FieldType.DECIMAL,
            "마이너스비즈월렛상계"),

    SAFE_RETURN_CARE_COST("반품안심케어비용", FieldType.DECIMAL,
            "반품안심케어비용"),

    PREFERRED_FEE_REFUND_AMOUNT("우대수수료환급", FieldType.DECIMAL,
            "우대수수료환급"),

    SETTLEMENT_METHOD("정산방식", FieldType.STRING,
            "정산방식"),

    NPAY_FEE_AMOUNT("Npay 수수료", FieldType.DECIMAL,
            "Npay 수수료", "Npay수수료"),

    SALES_LINKED_FEE_TOTAL("매출 연동 수수료 합계", FieldType.DECIMAL,
            "매출 연동 수수료 합계", "매출연동수수료합계"),

    INSTALLMENT_FEE_AMOUNT("무이자할부 수수료", FieldType.DECIMAL,
            "무이자할부 수수료", "무이자할부수수료"),

    BENEFIT_AMOUNT("혜택금액", FieldType.DECIMAL,
            "혜택금액"),

    SETTLEMENT_EXPECTED_AMOUNT("정산예정금액", FieldType.DECIMAL,
            "정산예정금액"),

    FEE_BASE_AMOUNT("수수료 기준금액", FieldType.DECIMAL,
            "수수료 기준금액", "수수료기준금액"),

    FEE_TYPE("수수료 구분", FieldType.STRING,
            "수수료 구분", "수수료구분"),

    PAYMENT_METHOD("결제수단", FieldType.STRING,
            "결제수단"),

    SALES_LINKED_FEE_DETAIL("매출연동수수료 상세", FieldType.STRING,
            "매출연동수수료 상세", "매출 연동 수수료 상세"),

    FEE_CAP_AMOUNT("수수료상한액", FieldType.DECIMAL,
            "수수료상한액"),

    COMMISSION_AMOUNT("수수료금액", FieldType.DECIMAL,
            "수수료금액");

    private final String label;
    private final FieldType fieldType;
    private final List<String> headerAliases;

    StandardSettlementField(String label, FieldType fieldType, String... headerAliases) {
        this.label = label;
        this.fieldType = fieldType;
        this.headerAliases = Arrays.stream(headerAliases)
                .map(StandardSettlementField::normalizeHeader)
                .distinct()
                .toList();
    }

    public String getLabel() {
        return label;
    }

    public FieldType getFieldType() {
        return fieldType;
    }

    public List<String> getHeaderAliases() {
        return headerAliases;
    }

    public boolean matches(String headerName) {
        String normalized = normalizeHeader(headerName);
        return headerAliases.contains(normalized);
    }

    public static String normalizeHeader(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\uFEFF", "")
                .replace("\n", "")
                .replace("\r", "")
                .replace(" ", "")
                .replace("\t", "")
                .trim()
                .toLowerCase();
    }
}