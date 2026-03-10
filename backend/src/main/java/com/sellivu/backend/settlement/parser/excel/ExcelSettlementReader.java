package com.sellivu.backend.settlement.parser.excel;

import com.sellivu.backend.settlement.parser.SettlementRawRow;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.web.multipart.MultipartFile;

public class ExcelSettlementReader {

    private final ExcelHeaderExtractor headerExtractor = new ExcelHeaderExtractor();

    public ExcelReadResult read(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(inputStream)) {

            if (workbook.getNumberOfSheets() == 0) {
                throw new IllegalArgumentException("엑셀 시트가 없습니다.");
            }

            Sheet sheet = workbook.getSheetAt(0);
            ExcelHeaderExtractor.HeaderExtractResult headerResult = headerExtractor.extract(sheet);

            List<String> headers = headerResult.headers();
            List<SettlementRawRow> rows = extractDataRows(sheet, headerResult.headerRowIndex(), headers);

            return new ExcelReadResult(headers, rows);

        } catch (IOException e) {
            throw new IllegalStateException("엑셀 파일을 읽는 중 오류가 발생했습니다.", e);
        }
    }

    private List<SettlementRawRow> extractDataRows(Sheet sheet, int headerRowIndex, List<String> headers) {
        List<SettlementRawRow> result = new ArrayList<>();

        for (int rowIndex = headerRowIndex + 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null) {
                continue;
            }

            Map<String, String> valuesByHeader = new LinkedHashMap<>();
            boolean hasAnyValue = false;

            for (int colIndex = 0; colIndex < headers.size(); colIndex++) {
                String header = headers.get(colIndex);
                String value = ExcelCellValueReader.readAsString(row.getCell(colIndex));
                valuesByHeader.put(header, value);

                if (!value.isBlank()) {
                    hasAnyValue = true;
                }
            }

            if (!hasAnyValue) {
                continue;
            }

            result.add(new SettlementRawRow(rowIndex + 1, valuesByHeader));
        }

        return result;
    }

    public record ExcelReadResult(
            List<String> headers,
            List<SettlementRawRow> rows
    ) {
    }
}