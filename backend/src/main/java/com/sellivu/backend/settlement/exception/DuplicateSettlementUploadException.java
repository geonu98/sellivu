package com.sellivu.backend.settlement.exception;

import com.sellivu.backend.global.error.ApiException;
import org.springframework.http.HttpStatus;

public class DuplicateSettlementUploadException extends ApiException {

    public DuplicateSettlementUploadException(String message) {
        super(
                HttpStatus.CONFLICT,
                "DUPLICATE_SETTLEMENT_UPLOAD",
                message
        );
    }
}