package com.sellivu.backend.settlement.parser;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SettlementValueParser {

    private static final List<DateTimeFormatter> DATE_FORMATTERS = List.of(
            DateTimeFormatter.ofPattern("yyyy.MM.dd"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("yyyy/M/d"),
            DateTimeFormatter.ofPattern("yyyy/M/dd"),
            DateTimeFormatter.ofPattern("yyyy.MM.d"),
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

        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
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

        normalized = normalized.replace(",", "")
                .replace("원", "")
                .replace("₩", "")
                .replace(" ", "");

        if (normalized.isBlank() || "-".equals(normalized)) {
            return null;
        }

        return normalized;
    }
}