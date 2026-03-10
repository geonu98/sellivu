package com.sellivu.backend.settlement.parser;

import com.sellivu.backend.settlement.domain.SettlementFileType;
import com.sellivu.backend.settlement.domain.StandardSettlementField;
import com.sellivu.backend.settlement.parser.csv.CsvSettlementReader;
import com.sellivu.backend.settlement.parser.excel.ExcelSettlementReader;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
 
// csv 파일도 지원
@Component
public class SettlementExcelParser {

    private final ExcelSettlementReader excelSettlementReader = new ExcelSettlementReader();
    private final CsvSettlementReader csvSettlementReader = new CsvSettlementReader();
    private final SettlementFileDetector settlementFileDetector = new SettlementFileDetector();
    private final SettlementHeaderMapper settlementHeaderMapper = new SettlementHeaderMapper();
    private final SettlementHeaderValidator settlementHeaderValidator = new SettlementHeaderValidator();
    private final SettlementRowMapper settlementRowMapper = new SettlementRowMapper();

    public SettlementParseResult parse(MultipartFile file) {
        ReaderResult readerResult = read(file);

        List<String> headers = readerResult.headers();
        SettlementFileDetectionResult detectionResult = settlementFileDetector.detect(headers);
        SettlementFileType fileType = detectionResult.getFileType();

        Map<String, StandardSettlementField> headerMapping =
                settlementHeaderMapper.mapHeaders(headers);

        settlementHeaderValidator.validateRequiredHeaders(fileType, headerMapping);

        List<SettlementParsedRow> parsedRows = readerResult.rows().stream()
                .map(rawRow -> settlementRowMapper.map(rawRow, headerMapping))
                .toList();

        return new SettlementParseResult(
                fileType,
                headers,
                headerMapping,
                parsedRows
        );
    }

    private ReaderResult read(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String lower = originalFilename == null ? "" : originalFilename.toLowerCase(Locale.ROOT);

        if (lower.endsWith(".csv")) {
            CsvSettlementReader.CsvReadResult result = csvSettlementReader.read(file);
            return new ReaderResult(result.headers(), result.rows());
        }

        ExcelSettlementReader.ExcelReadResult result = excelSettlementReader.read(file);
        return new ReaderResult(result.headers(), result.rows());
    }

    private record ReaderResult(
            List<String> headers,
            List<SettlementRawRow> rows
    ) {
    }
}