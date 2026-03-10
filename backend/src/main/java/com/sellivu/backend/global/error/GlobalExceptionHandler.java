package com.sellivu.backend.global.error;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(
            ApiException e,
            HttpServletRequest request
    ) {
        log.error("ApiException occurred. path={}, code={}, message={}",
                request.getRequestURI(),
                e.getCode(),
                e.getMessage(),
                e);

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(e.getStatus().value())
                .error(e.getStatus().getReasonPhrase())
                .code(e.getCode())
                .message(e.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(e.getStatus()).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException e,
            HttpServletRequest request
    ) {
        log.error("Validation exception. path={}", request.getRequestURI(), e);

        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(fieldError -> fieldError.getDefaultMessage())
                .orElse(ErrorCode.INVALID_INPUT_VALUE.getMessage());

        return buildResponse(
                ErrorCode.INVALID_INPUT_VALUE.getStatus(),
                ErrorCode.INVALID_INPUT_VALUE.getCode(),
                message,
                request.getRequestURI()
        );
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMediaTypeException(
            HttpMediaTypeNotSupportedException e,
            HttpServletRequest request
    ) {
        log.error("Unsupported media type. path={}", request.getRequestURI(), e);

        return buildResponse(
                ErrorCode.UNSUPPORTED_MEDIA_TYPE.getStatus(),
                ErrorCode.UNSUPPORTED_MEDIA_TYPE.getCode(),
                ErrorCode.UNSUPPORTED_MEDIA_TYPE.getMessage(),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException e,
            HttpServletRequest request
    ) {
        log.error("HttpMessageNotReadableException. path={}", request.getRequestURI(), e);

        return buildResponse(
                ErrorCode.INVALID_INPUT_VALUE.getStatus(),
                ErrorCode.INVALID_INPUT_VALUE.getCode(),
                "요청 본문(JSON) 형식이 올바르지 않습니다.",
                request.getRequestURI()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(
            Exception e,
            HttpServletRequest request
    ) {
        log.error("Unhandled exception. path={}", request.getRequestURI(), e);

        return buildResponse(
                ErrorCode.INTERNAL_SERVER_ERROR.getStatus(),
                ErrorCode.INTERNAL_SERVER_ERROR.getCode(),
                ErrorCode.INTERNAL_SERVER_ERROR.getMessage(),
                request.getRequestURI()
        );
    }

    private ResponseEntity<ErrorResponse> buildResponse(
            HttpStatus status,
            String code,
            String message,
            String path
    ) {
        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .code(code)
                .message(message)
                .path(path)
                .build();

        return ResponseEntity.status(status).body(response);
    }
}