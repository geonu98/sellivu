package com.sellivu.backend.settlement.parser;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SettlementRawRow {

    private final int rowNumber;
    private final Map<String, String> valuesByHeader;

    public SettlementRawRow(int rowNumber, Map<String, String> valuesByHeader) {
        this.rowNumber = rowNumber;
        this.valuesByHeader = valuesByHeader != null
                ? new HashMap<>(valuesByHeader)
                : new HashMap<>();
    }

    public int getRowNumber() {
        return rowNumber;
    }

    public Map<String, String> getValuesByHeader() {
        return Collections.unmodifiableMap(valuesByHeader);
    }

    public String getValue(String headerName) {
        if (headerName == null) {
            return null;
        }
        return valuesByHeader.get(headerName);
    }

    public boolean hasValue(String headerName) {
        String value = getValue(headerName);
        return value != null && !value.trim().isEmpty();
    }
}