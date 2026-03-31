package com.sellivu.backend.settlement.parser;

import com.sellivu.backend.settlement.domain.SettlementFileType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SettlementFileDetectorTest {

    private final SettlementFileDetector detector = new SettlementFileDetector();

    @Test
    @DisplayName("DAILY_SETTLEMENT 필수 헤더를 모두 포함하면 일별 정산 파일로 판별한다")
    void detect_shouldReturnDailySettlement() {
        List<String> headers = List.of(
                "정산예정일",
                "정산완료일",
                "정산금액",
                "일반정산금액",
                "빠른정산금액",
                "정산기준금액",
                "수수료합계",
                "정산방식"
        );

        SettlementFileDetectionResult result = detector.detect(headers);
        assertEquals(headers, result.getHeaders());
        assertEquals(SettlementFileType.DAILY_SETTLEMENT, result.getFileType());
    }

    @Test
    @DisplayName("FEE_DETAIL 필수 헤더를 모두 포함하면 수수료 상세 파일로 판별한다")
    void detect_shouldReturnFeeDetail() {
        List<String> headers = List.of(
                "주문번호",
                "상품주문번호",
                "수수료기준금액",
                "수수료구분",
                "결제수단",
                "매출연동수수료상세",
                "수수료상한액",
                "수수료금액"
        );

        SettlementFileDetectionResult result = detector.detect(headers);
        assertEquals(SettlementFileType.FEE_DETAIL, result.getFileType());
        assertEquals(headers, result.getHeaders());;
    }

    @Test
    @DisplayName("ORDER_SETTLEMENT 필수 헤더를 모두 포함하면 주문 정산 파일로 판별한다")
    void detect_shouldReturnOrderSettlement() {
        List<String> headers = List.of(
                "주문번호",
                "상품주문번호",
                "결제일",
                "금액변동일",
                "정산기준금액",
                "npay수수료",
                "매출연동수수료합계",
                "정산예정금액"
        );

        SettlementFileDetectionResult result = detector.detect(headers);

        assertEquals(SettlementFileType.ORDER_SETTLEMENT, result.getFileType());
        assertEquals(headers, result.getHeaders());
    }

    @Test
    @DisplayName("헤더가 null이면 예외를 던진다")
    void detect_shouldThrowWhenHeadersAreNull() {
        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> detector.detect(null));

        assertTrue(exception.getMessage().contains("헤더가 비어 있어"));
    }

    @Test
    @DisplayName("헤더가 비어 있으면 예외를 던진다")
    void detect_shouldThrowWhenHeadersAreEmpty() {
        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> detector.detect(List.of()));

        assertTrue(exception.getMessage().contains("헤더가 비어 있어"));
    }

    @Test
    @DisplayName("필수 헤더가 부족하면 지원하지 않는 형식 예외를 던진다")
    void detect_shouldThrowWhenUnsupportedHeaders() {
        List<String> headers = List.of(
                "주문번호",
                "상품주문번호",
                "아무헤더",
                "테스트"
        );

        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> detector.detect(headers));

        assertTrue(exception.getMessage().contains("지원하지 않는 정산 파일 형식"));
    }

    @Test
    @DisplayName("헤더 공백 차이가 있어도 정규화 후 판별할 수 있다")
    void detect_shouldDetectAfterHeaderNormalization() {
        List<String> headers = List.of(
                " 주문번호 ",
                " 상품주문번호 ",
                " 결제일 ",
                " 금액변동일 ",
                " 정산기준금액 ",
                " npay수수료 ",
                " 매출연동수수료합계 ",
                " 정산예정금액 "
        );

        SettlementFileDetectionResult result = detector.detect(headers);

        assertEquals(SettlementFileType.ORDER_SETTLEMENT, result.getFileType());
    }
}