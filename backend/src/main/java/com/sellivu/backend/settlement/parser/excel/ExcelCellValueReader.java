package com.sellivu.backend.settlement.parser.excel;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;

public final class ExcelCellValueReader {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.############################");

    private ExcelCellValueReader() {
    }

    public static String readAsString(Cell cell) {
        if (cell == null) {
            return "";
        }

        CellType cellType = cell.getCellType();
        if (cellType == CellType.FORMULA) {
            cellType = cell.getCachedFormulaResultType();
        }

        return switch (cellType) {
            case STRING -> safeTrim(cell.getStringCellValue());
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    LocalDate localDate = cell.getDateCellValue()
                            .toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();
                    yield DATE_FORMATTER.format(localDate);
                }
                yield numberToString(cell.getNumericCellValue());
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case BLANK -> "";
            default -> safeTrim(cell.toString());
        };
    }

    private static String numberToString(double value) {
        BigDecimal decimal = BigDecimal.valueOf(value);
        return safeTrim(DECIMAL_FORMAT.format(decimal));
    }

    private static String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }
}