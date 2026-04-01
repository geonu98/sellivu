package com.sellivu.backend.settlement.parser;

import com.sellivu.backend.settlement.domain.SettlementFileType;
import com.sellivu.backend.settlement.domain.StandardSettlementField;
import com.sellivu.backend.settlement.parser.csv.CsvSettlementReader;
import com.sellivu.backend.settlement.parser.excel.ExcelSettlementReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
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
        return buildParseResult(file.getOriginalFilename(), readerResult);
    }

    public SettlementParseResult parse(String originalFileName, InputStream inputStream) throws IOException {
        ReaderResult readerResult = read(originalFileName, inputStream);
        return buildParseResult(originalFileName, readerResult);
    }

    private SettlementParseResult buildParseResult(String originalFileName, ReaderResult readerResult) {
        List<String> headers = readerResult.headers();

        long detectStart = System.currentTimeMillis();
        SettlementFileDetectionResult detectionResult = settlementFileDetector.detect(headers);
        SettlementFileType fileType = detectionResult.getFileType();
        log.info(
                "[PERF] excelParser.detect originalFileName={} fileType={} headerCount={} took={}ms",
                originalFileName,
                fileType,
                headers.size(),
                System.currentTimeMillis() - detectStart
        );

        long headerMapStart = System.currentTimeMillis();
        Map<String, StandardSettlementField> headerMapping =
                settlementHeaderMapper.mapHeaders(headers);
        log.info(
                "[PERF] excelParser.mapHeaders originalFileName={} mappedHeaders={} took={}ms",
                originalFileName,
                headerMapping.size(),
                System.currentTimeMillis() - headerMapStart
        );

        long validateStart = System.currentTimeMillis();
        settlementHeaderValidator.validateRequiredHeaders(fileType, headerMapping);
        log.info(
                "[PERF] excelParser.validateHeaders originalFileName={} fileType={} took={}ms",
                originalFileName,
                fileType,
                System.currentTimeMillis() - validateStart
        );

        long rowMapStart = System.currentTimeMillis();
        List<SettlementParsedRow> parsedRows = readerResult.rows().stream()
                .map(rawRow -> settlementRowMapper.map(rawRow, headerMapping))
                .toList();
        log.info(
                "[PERF] excelParser.mapRows originalFileName={} rowCount={} took={}ms",
                originalFileName,
                parsedRows.size(),
                System.currentTimeMillis() - rowMapStart
        );

        return new SettlementParseResult(
                fileType,
                headers,
                headerMapping,
                parsedRows
        );
    }

    private ReaderResult read(MultipartFile file) {
        long readStart = System.currentTimeMillis();
        String originalFilename = file.getOriginalFilename();
        String lower = originalFilename == null ? "" : originalFilename.toLowerCase(Locale.ROOT);

        ReaderResult result;
        if (lower.endsWith(".csv")) {
            CsvSettlementReader.CsvReadResult csvResult = csvSettlementReader.read(file);
            result = new ReaderResult(csvResult.headers(), csvResult.rows());
        } else {
            ExcelSettlementReader.ExcelReadResult excelResult = excelSettlementReader.read(file);
            result = new ReaderResult(excelResult.headers(), excelResult.rows());
        }

        log.info(
                "[PERF] excelParser.read originalFileName={} rowCount={} took={}ms",
                originalFilename,
                result.rows().size(),
                System.currentTimeMillis() - readStart
        );
        return result;
    }

    private ReaderResult read(String originalFileName, InputStream inputStream) throws IOException {
        long readStart = System.currentTimeMillis();
        String lower = originalFileName == null ? "" : originalFileName.toLowerCase(Locale.ROOT);

        ReaderResult result;
        if (lower.endsWith(".csv")) {
            CsvSettlementReader.CsvReadResult csvResult = csvSettlementReader.read(inputStream);
            result = new ReaderResult(csvResult.headers(), csvResult.rows());
        } else {
            ExcelSettlementReader.ExcelReadResult excelResult = excelSettlementReader.read(inputStream);
            result = new ReaderResult(excelResult.headers(), excelResult.rows());
        }

        log.info(
                "[PERF] excelParser.read originalFileName={} rowCount={} took={}ms",
                originalFileName,
                result.rows().size(),
                System.currentTimeMillis() - readStart
        );
        return result;
    }

    private record ReaderResult(
            List<String> headers,
            List<SettlementRawRow> rows
    ) {
    }
}
