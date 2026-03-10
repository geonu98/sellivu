package com.sellivu.backend.settlement.parser;

import com.sellivu.backend.settlement.domain.SettlementFileType;
import com.sellivu.backend.settlement.domain.StandardSettlementField;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SettlementParseResult {

    private final SettlementFileType fileType;
    private final List<String> originalHeaders;
    private final Map<String, StandardSettlementField> headerMapping;
    private final List<SettlementParsedRow> rows;

    public SettlementParseResult(
            SettlementFileType fileType,
            List<String> originalHeaders,
            Map<String, StandardSettlementField> headerMapping,
            List<SettlementParsedRow> rows
    ) {
        this.fileType = fileType;
        this.originalHeaders = Collections.unmodifiableList(originalHeaders);
        this.headerMapping = Collections.unmodifiableMap(headerMapping);
        this.rows = Collections.unmodifiableList(rows);
    }

    public SettlementFileType getFileType() {
        return fileType;
    }

    public List<String> getOriginalHeaders() {
        return originalHeaders;
    }

    public Map<String, StandardSettlementField> getHeaderMapping() {
        return headerMapping;
    }

    public List<SettlementParsedRow> getRows() {
        return rows;
    }
}