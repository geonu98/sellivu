package com.sellivu.backend.settlement.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class SettlementValueParserTest {

    private final SettlementValueParser parser = new SettlementValueParser();

    @Test
    @DisplayName("asString: null, 공백 문자열은 null을 반환하고 trim 처리한다")
    void asString_shouldTrimAndReturnNullForBlank() {
        assertNull(parser.asString(null));
        assertNull(parser.asString(""));
        assertNull(parser.asString("   "));
        assertEquals("abc", parser.asString("  abc  "));
    }

    @Test
    @DisplayName("asInteger: 콤마, 원, 공백을 제거하고 정수로 파싱한다")
    void asInteger_shouldParseNormalizedNumber() {
        assertEquals(1234, parser.asInteger("1,234"));
        assertEquals(1234, parser.asInteger("1,234원"));
        assertEquals(1234, parser.asInteger(" ₩1,234 "));
        assertEquals(1234, parser.asInteger("1234.99"));
    }

    @Test
    @DisplayName("asInteger: null, 공백, 대시 입력은 null을 반환한다")
    void asInteger_shouldReturnNullForEmptyLikeValues() {
        assertNull(parser.asInteger(null));
        assertNull(parser.asInteger(""));
        assertNull(parser.asInteger("   "));
        assertNull(parser.asInteger("-"));
    }

    @Test
    @DisplayName("asLong: 큰 수를 정상 파싱한다")
    void asLong_shouldParseNormalizedNumber() {
        assertEquals(12345678901L, parser.asLong("12,345,678,901"));
        assertEquals(5000L, parser.asLong("5,000원"));
        assertEquals(7000L, parser.asLong(" ₩7,000 "));
        assertEquals(123L, parser.asLong("123.45"));
    }

    @Test
    @DisplayName("asBigDecimal: 콤마, 원, 원화기호, 공백 제거 후 소수까지 유지한다")
    void asBigDecimal_shouldParseDecimal() {
        assertEquals(new BigDecimal("1234.56"), parser.asBigDecimal("1,234.56"));
        assertEquals(new BigDecimal("1234.56"), parser.asBigDecimal("1,234.56원"));
        assertEquals(new BigDecimal("1234.56"), parser.asBigDecimal(" ₩1,234.56 "));
        assertEquals(new BigDecimal("-1234.56"), parser.asBigDecimal("-1,234.56"));
    }

    @Test
    @DisplayName("asBigDecimal: null, 공백, 대시 입력은 null을 반환한다")
    void asBigDecimal_shouldReturnNullForEmptyLikeValues() {
        assertNull(parser.asBigDecimal(null));
        assertNull(parser.asBigDecimal(""));
        assertNull(parser.asBigDecimal("   "));
        assertNull(parser.asBigDecimal("-"));
    }

    @Test
    @DisplayName("asLocalDate: yyyy-MM-dd 형식을 파싱한다")
    void asLocalDate_shouldParseDashFormat() {
        assertEquals(LocalDate.of(2026, 3, 31), parser.asLocalDate("2026-03-31"));
    }

    @Test
    @DisplayName("asLocalDate: yyyy.MM.dd, yyyy.MM.d 형식을 파싱한다")
    void asLocalDate_shouldParseDotFormats() {
        assertEquals(LocalDate.of(2026, 3, 31), parser.asLocalDate("2026.03.31"));
        assertEquals(LocalDate.of(2026, 3, 1), parser.asLocalDate("2026.03.1"));
    }

    @Test
    @DisplayName("asLocalDate: yyyy/M/d, yyyy/M/dd 형식을 파싱한다")
    void asLocalDate_shouldParseSlashFormats() {
        assertEquals(LocalDate.of(2026, 3, 1), parser.asLocalDate("2026/3/1"));
        assertEquals(LocalDate.of(2026, 3, 31), parser.asLocalDate("2026/3/31"));
    }

    @Test
    @DisplayName("asLocalDate: BASIC_ISO_DATE 형식을 파싱한다")
    void asLocalDate_shouldParseBasicIsoDate() {
        assertEquals(LocalDate.of(2026, 3, 31), parser.asLocalDate("20260331"));
    }

    @Test
    @DisplayName("asLocalDate: null, 공백 문자열은 null을 반환한다")
    void asLocalDate_shouldReturnNullForBlank() {
        assertNull(parser.asLocalDate(null));
        assertNull(parser.asLocalDate(""));
        assertNull(parser.asLocalDate("   "));
    }

    @Test
    @DisplayName("asLocalDate: 지원하지 않는 형식이면 예외를 던진다")
    void asLocalDate_shouldThrowForInvalidFormat() {
        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> parser.asLocalDate("31-03-2026"));

        assertTrue(exception.getMessage().contains("날짜 파싱에 실패했습니다."));
    }
}