package com.sellivu.backend.settlement.exception;

import com.sellivu.backend.global.error.ApiException;
import org.springframework.http.HttpStatus;

public class AnalysisSetUploadAlreadyExistsException extends ApiException {

    public AnalysisSetUploadAlreadyExistsException(Long uploadId) {
        super(
                HttpStatus.CONFLICT,
                "ANALYSIS_SET_UPLOAD_ALREADY_EXISTS",
                "이미 해당 분석 세트에 포함된 업로드입니다. uploadId=" + uploadId
        );
    }
}