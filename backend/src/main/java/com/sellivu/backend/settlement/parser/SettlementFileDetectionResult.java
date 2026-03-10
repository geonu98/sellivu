package com.sellivu.backend.settlement.parser;

import com.sellivu.backend.settlement.domain.SettlementFileType;
import java.util.Collections;
import java.util.List;

public class SettlementFileDetectionResult {

    private final SettlementFileType fileType;
    private final List<String> headers;

    public SettlementFileDetectionResult(SettlementFileType fileType, List<String> headers) {
        this.fileType = fileType;
        this.headers = Collections.unmodifiableList(headers);
    }

    public SettlementFileType getFileType() {
        return fileType;
    }

    public List<String> getHeaders() {
        return headers;
    }
}