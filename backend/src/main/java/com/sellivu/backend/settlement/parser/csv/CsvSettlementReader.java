package com.sellivu.backend.settlement.parser.csv;

import com.sellivu.backend.settlement.parser.SettlementRawRow;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.multipart.MultipartFile;

public class CsvSettlementReader {

    public CsvReadResult read(MultipartFile file) {
        try {
            List<String> lines = readAllLines(file);

            if (lines.isEmpty()) {
                throw new IllegalArgumentException("CSV 파일이 비어 있습니다.");
            }

            int headerRowIndex = findHeaderRowIndex(lines);
            if (headerRowIndex < 0) {
                throw new IllegalArgumentException("CSV 헤더 행을 찾을 수 없습니다.");
            }

            List<String> headers = normalizeHeaders(parseCsvLine(lines.get(headerRowIndex)));
            if (headers.isEmpty()) {
                throw new IllegalArgumentException("CSV 헤더가 비어 있습니다.");
            }

            List<SettlementRawRow> rows = extractDataRows(lines, headerRowIndex, headers);
            return new CsvReadResult(headers, rows);

        } catch (IOException e) {
            throw new IllegalStateException("CSV 파일을 읽는 중 오류가 발생했습니다.", e);
        }
    }

    private List<String> readAllLines(MultipartFile file) throws IOException {
        byte[] bytes = file.getBytes();

        List<String> utf8Lines = readLines(bytes, StandardCharsets.UTF_8);
        if (looksBroken(utf8Lines)) {
            return readLines(bytes, Charset.forName("MS949"));
        }
        return utf8Lines;
    }

    private List<String> readLines(byte[] bytes, Charset charset) throws IOException {
        List<String> lines = new ArrayList<>();

        try (InputStream inputStream = new java.io.ByteArrayInputStream(bytes);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, charset))) {

            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(removeBom(line));
            }
        }

        return lines;
    }

    private boolean looksBroken(List<String> lines) {
        if (lines.isEmpty()) {
            return false;
        }

        String firstNonBlank = lines.stream()
                .filter(line -> line != null && !line.isBlank())
                .findFirst()
                .orElse("");

        return firstNonBlank.contains("�");
    }

    private int findHeaderRowIndex(List<String> lines) {
        for (int i = 0; i < lines.size(); i++) {
            List<String> values = normalizeHeaders(parseCsvLine(lines.get(i)));
            long nonBlankCount = values.stream()
                    .filter(v -> v != null && !v.isBlank())
                    .count();

            if (nonBlankCount >= 2) {
                return i;
            }
        }
        return -1;
    }

    private List<SettlementRawRow> extractDataRows(
            List<String> lines,
            int headerRowIndex,
            List<String> headers
    ) {
        List<SettlementRawRow> result = new ArrayList<>();

        for (int lineIndex = headerRowIndex + 1; lineIndex < lines.size(); lineIndex++) {
            String line = lines.get(lineIndex);
            if (line == null || line.isBlank()) {
                continue;
            }

            List<String> values = parseCsvLine(line);
            Map<String, String> valuesByHeader = new LinkedHashMap<>();
            boolean hasAnyValue = false;

            for (int colIndex = 0; colIndex < headers.size(); colIndex++) {
                String header = headers.get(colIndex);
                String value = colIndex < values.size() ? nullSafeTrim(values.get(colIndex)) : "";
                valuesByHeader.put(header, value);

                if (!value.isBlank()) {
                    hasAnyValue = true;
                }
            }

            if (!hasAnyValue) {
                continue;
            }

            result.add(new SettlementRawRow(lineIndex + 1, valuesByHeader));
        }

        return result;
    }

    private List<String> parseCsvLine(String line) {
        List<String> values = new ArrayList<>();
        if (line == null) {
            return values;
        }

        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);

            if (ch == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
                continue;
            }

            if (ch == ',' && !inQuotes) {
                values.add(current.toString());
                current.setLength(0);
                continue;
            }

            current.append(ch);
        }

        values.add(current.toString());
        return values;
    }

    private List<String> normalizeHeaders(List<String> headers) {
        List<String> normalized = new ArrayList<>(headers.size());

        for (String header : headers) {
            normalized.add(nullSafeTrim(removeBom(header)));
        }

        trimTrailingEmpty(normalized);
        return normalized;
    }

    private void trimTrailingEmpty(List<String> values) {
        for (int i = values.size() - 1; i >= 0; i--) {
            if (values.get(i) == null || values.get(i).isBlank()) {
                values.remove(i);
            } else {
                break;
            }
        }
    }

    private String removeBom(String value) {
        if (value == null || value.isEmpty()) {
            return value == null ? "" : value;
        }
        if (value.charAt(0) == '\uFEFF') {
            return value.substring(1);
        }
        return value;
    }

    private String nullSafeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    public record CsvReadResult(
            List<String> headers,
            List<SettlementRawRow> rows
    ) {
    }
}