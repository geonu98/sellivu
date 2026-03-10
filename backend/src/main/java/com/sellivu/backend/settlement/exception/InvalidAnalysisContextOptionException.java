package com.sellivu.backend.settlement.exception;

import com.sellivu.backend.global.error.ApiException;
import org.springframework.http.HttpStatus;

public class InvalidAnalysisContextOptionException extends ApiException {

    public InvalidAnalysisContextOptionException(String field, String value) {
        super(
                HttpStatus.BAD_REQUEST,
                "INVALID_ANALYSIS_CONTEXT_OPTION",
                "잘못된 context 옵션 값입니다. field=" + field + ", value=" + value
        );
    }
}