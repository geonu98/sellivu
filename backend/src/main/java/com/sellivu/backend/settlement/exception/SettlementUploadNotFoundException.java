package com.sellivu.backend.settlement.exception;

import com.sellivu.backend.global.error.ApiException;
import org.springframework.http.HttpStatus;

public class SettlementUploadNotFoundException extends ApiException {

    public SettlementUploadNotFoundException(Long uploadId) {
        super(
                HttpStatus.NOT_FOUND,
                "SETTLEMENT_UPLOAD_NOT_FOUND",
                "해당 업로드를 찾을 수 없습니다. id=" + uploadId
        );
    }
}