package com.sellivu.backend.settlement.parser;

import com.sellivu.backend.settlement.domain.StandardSettlementField;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public class SettlementParsedRow {

    private final int rowNumber;
    private final Map<StandardSettlementField, String> values;

    public SettlementParsedRow(int rowNumber, Map<StandardSettlementField, String> values) {
        this.rowNumber = rowNumber;
        this.values = Collections.unmodifiableMap(new EnumMap<>(values));
    }

    public int getRowNumber() {
        return rowNumber;
    }

    public Map<StandardSettlementField, String> getValues() {
        return values;
    }

    public String get(StandardSettlementField field) {
        return values.get(field);
    }

    public boolean has(StandardSettlementField field) {
        String value = values.get(field);
        return value != null && !value.isBlank();
    }
}