package com.sellivu.backend.settlement.parser;

import com.sellivu.backend.settlement.domain.StandardSettlementField;
import java.util.EnumMap;
import java.util.Map;

public class SettlementRowMapper {

    public SettlementParsedRow map(
            SettlementRawRow rawRow,
            Map<String, StandardSettlementField> headerMapping
    ) {
        Map<StandardSettlementField, String> values = new EnumMap<>(StandardSettlementField.class);

        for (Map.Entry<String, StandardSettlementField> entry : headerMapping.entrySet()) {
            String originalHeader = entry.getKey();
            StandardSettlementField field = entry.getValue();
            String rawValue = rawRow.getValue(originalHeader);

            values.put(field, normalizeCellValue(rawValue));
        }

        return new SettlementParsedRow(rawRow.getRowNumber(), values);
    }

    private String normalizeCellValue(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }
}