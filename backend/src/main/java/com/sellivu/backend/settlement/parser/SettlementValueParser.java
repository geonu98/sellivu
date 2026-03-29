package com.sellivu.backend.settlement.parser;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class SettlementValueParser {

    private static final DateTimeFormatter DATE_DOT_2 = DateTimeFormatter.ofPattern("yyyy.MM.dd");
    private static final DateTimeFormatter DATE_DASH = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_SLASH_1 = DateTimeFormatter.ofPattern("yyyy/M/d");
    private static final DateTimeFormatter DATE_SLASH_2 = DateTimeFormatter.ofPattern("yyyy/M/dd");
    private static final DateTimeFormatter DATE_DOT_1 = DateTimeFormatter.ofPattern("yyyy.MM.d");

    private static final List<DateTimeFormatter> FALLBACK_DATE_FORMATTERS = List.of(
            DATE_DOT_2,
            DATE_DASH,
            DATE_SLASH_1,
            DATE_SLASH_2,
            DATE_DOT_1,
            DateTimeFormatter.BASIC_ISO_DATE
    );

    public String asString(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }

    public Integer asInteger(String value) {
        String normalized = normalizeNumber(value);
        if (normalized == null) {
            return null;
        }
        return Integer.parseInt(normalized);
    }

    public Long asLong(String value) {
        String normalized = normalizeNumber(value);
        if (normalized == null) {
            return null;
        }
        return Long.parseLong(normalized);
    }

    public BigDecimal asBigDecimal(String value) {
        String normalized = normalizeDecimal(value);
        if (normalized == null) {
            return null;
        }
        return new BigDecimal(normalized);
    }

    public LocalDate asLocalDate(String value) {
        String normalized = asString(value);
        if (normalized == null) {
            return null;
        }

        int length = normalized.length();

        try {
            if (length == 10) {
                char c4 = normalized.charAt(4);
                char c7 = normalized.charAt(7);

                if (c4 == '-' && c7 == '-') {
                    return LocalDate.parse(normalized, DATE_DASH);
                }

                if (c4 == '.' && c7 == '.') {
                    return LocalDate.parse(normalized, DATE_DOT_2);
                }
            }

            if (length == 9) {
                if (normalized.charAt(4) == '.' && normalized.charAt(7) == '.') {
                    return LocalDate.parse(normalized, DATE_DOT_1);
                }

                if (normalized.charAt(4) == '/' && normalized.charAt(6) == '/') {
                    return LocalDate.parse(normalized, DATE_SLASH_1);
                }
            }

            if (length == 8 && isDigitsOnly(normalized)) {
                return LocalDate.parse(normalized, DateTimeFormatter.BASIC_ISO_DATE);
            }

            if (normalized.indexOf('/') >= 0) {
                return LocalDate.parse(normalized, DATE_SLASH_2);
            }
        } catch (Exception ignored) {
            // 아래 fallback으로 이동
        }

        for (DateTimeFormatter formatter : FALLBACK_DATE_FORMATTERS) {
            try {
                return LocalDate.parse(normalized, formatter);
            } catch (Exception ignored) {
            }
        }

        throw new IllegalArgumentException("날짜 파싱에 실패했습니다. value=" + value);
    }

    private String normalizeNumber(String value) {
        String normalized = normalizeDecimal(value);
        if (normalized == null) {
            return null;
        }

        int dotIndex = normalized.indexOf('.');
        if (dotIndex >= 0) {
            normalized = normalized.substring(0, dotIndex);
        }
        return normalized;
    }

    private String normalizeDecimal(String value) {
        String normalized = asString(value);
        if (normalized == null) {
            return null;
        }

        if (normalized.indexOf(',') >= 0) {
            normalized = normalized.replace(",", "");
        }
        if (normalized.indexOf('원') >= 0) {
            normalized = normalized.replace("원", "");
        }
        if (normalized.indexOf('₩') >= 0) {
            normalized = normalized.replace("₩", "");
        }
        if (normalized.indexOf(' ') >= 0) {
            normalized = normalized.replace(" ", "");
        }

        if (normalized.isBlank() || "-".equals(normalized)) {
            return null;
        }

        return normalized;
    }

    private boolean isDigitsOnly(String value) {
        for (int i = 0; i < value.length(); i++) {
            if (!Character.isDigit(value.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}