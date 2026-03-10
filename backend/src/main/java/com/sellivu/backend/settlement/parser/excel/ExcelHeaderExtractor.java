package com.sellivu.backend.settlement.parser.excel;

import java.util.ArrayList;
import java.util.List;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

public class ExcelHeaderExtractor {

    public HeaderExtractResult extract(Sheet sheet) {
        if (sheet == null) {
            throw new IllegalArgumentException("엑셀 시트가 없습니다.");
        }

        for (int rowIndex = sheet.getFirstRowNum(); rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null) {
                continue;
            }

            List<String> headers = extractRowValues(row);
            if (isHeaderRow(headers)) {
                return new HeaderExtractResult(rowIndex, headers);
            }
        }

        throw new IllegalArgumentException("헤더 행을 찾을 수 없습니다.");
    }

    private List<String> extractRowValues(Row row) {
        List<String> values = new ArrayList<>();
        int lastCellNum = Math.max(row.getLastCellNum(), 0);

        for (int cellIndex = 0; cellIndex < lastCellNum; cellIndex++) {
            String value = ExcelCellValueReader.readAsString(row.getCell(cellIndex));
            values.add(value);
        }

        trimTrailingEmpty(values);
        return values;
    }

    private boolean isHeaderRow(List<String> values) {
        long nonBlankCount = values.stream()
                .filter(v -> v != null && !v.isBlank())
                .count();
        return nonBlankCount >= 2;
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

    public record HeaderExtractResult(int headerRowIndex, List<String> headers) {
    }
}