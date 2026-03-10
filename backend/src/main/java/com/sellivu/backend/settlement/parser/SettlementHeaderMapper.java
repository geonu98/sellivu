package com.sellivu.backend.settlement.parser;

import com.sellivu.backend.settlement.domain.StandardSettlementField;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SettlementHeaderMapper {

    public Map<String, StandardSettlementField> mapHeaders(List<String> originalHeaders) {
        Map<String, StandardSettlementField> result = new LinkedHashMap<>();

        for (String header : originalHeaders) {
            StandardSettlementField matched = findField(header);
            if (matched != null) {
                result.put(header, matched);
            }
        }

        return result;
    }

    private StandardSettlementField findField(String header) {
        for (StandardSettlementField field : StandardSettlementField.values()) {
            if (field.matches(header)) {
                return field;
            }
        }
        return null;
    }
}