package com.sellivu.backend.settlement.exception;

import com.sellivu.backend.global.error.ApiException;
import org.springframework.http.HttpStatus;

public class AnalysisSetNotFoundException extends ApiException {

    public AnalysisSetNotFoundException(Long analysisSetId) {
        super(
                HttpStatus.NOT_FOUND,
                "ANALYSIS_SET_NOT_FOUND",
                "해당 분석 세트를 찾을 수 없습니다. id=" + analysisSetId
        );
    }
}