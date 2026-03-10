package com.sellivu.backend.settlement.parser;

import com.sellivu.backend.settlement.domain.SettlementFileType;
import com.sellivu.backend.settlement.domain.StandardSettlementField;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SettlementHeaderValidator {

    public void validateRequiredHeaders(
            SettlementFileType fileType,
            Map<String, StandardSettlementField> mappedHeaders
    ) {
        Set<StandardSettlementField> requiredFields = SettlementRequiredFields.getRequiredFields(fileType);
        List<String> missingLabels = new ArrayList<>();

        for (StandardSettlementField requiredField : requiredFields) {
            boolean exists = mappedHeaders.values().stream()
                    .anyMatch(mapped -> mapped == requiredField);

            if (!exists) {
                missingLabels.add(requiredField.getLabel());
            }
        }

        if (!missingLabels.isEmpty()) {
            throw new IllegalArgumentException(
                    "필수 헤더가 누락되었습니다. fileType=" + fileType + ", missing=" + missingLabels
            );
        }
    }
}