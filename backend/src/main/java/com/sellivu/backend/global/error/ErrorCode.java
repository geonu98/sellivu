package com.sellivu.backend.global.error;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_500", "서버 내부 오류가 발생했습니다."),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "COMMON_400", "잘못된 요청입니다."),
    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "COMMON_415", "지원하지 않는 Content-Type 입니다."),

    AUTH_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH_401", "인증이 필요합니다."),
    AUTH_INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH_INVALID_CREDENTIALS", "이메일 또는 비밀번호가 올바르지 않습니다."),
    AUTH_INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_INVALID_TOKEN", "유효하지 않은 토큰입니다."),
    AUTH_EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_EXPIRED_TOKEN", "만료된 토큰입니다."),
    AUTH_MISSING_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_MISSING_TOKEN", "인증 토큰이 없습니다."),
    AUTH_FORBIDDEN(HttpStatus.FORBIDDEN, "AUTH_403", "접근 권한이 없습니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "AUTH_REFRESH_TOKEN_NOT_FOUND", "리프레시 토큰을 찾을 수 없습니다."),
    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH_REFRESH_TOKEN_EXPIRED", "리프레시 토큰이 만료되었습니다."),
    REFRESH_TOKEN_MISMATCH(HttpStatus.UNAUTHORIZED, "AUTH_REFRESH_TOKEN_MISMATCH", "리프레시 토큰이 일치하지 않습니다."),

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_404", "사용자를 찾을 수 없습니다."),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "USER_EMAIL_CONFLICT", "이미 사용 중인 이메일입니다."),

    WORKSPACE_NOT_FOUND(HttpStatus.NOT_FOUND, "WORKSPACE_404", "워크스페이스를 찾을 수 없습니다."),
    WORKSPACE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "WORKSPACE_403", "워크스페이스 접근 권한이 없습니다."),
    WORKSPACE_EXPIRED(HttpStatus.GONE, "WORKSPACE_EXPIRED", "만료된 워크스페이스입니다."),
    WORKSPACE_NOT_ACTIVE(HttpStatus.BAD_REQUEST, "WORKSPACE_NOT_ACTIVE", "활성 상태의 워크스페이스가 아닙니다."),
    WORKSPACE_SAVE_REQUIRES_LOGIN(HttpStatus.UNAUTHORIZED, "WORKSPACE_SAVE_REQUIRES_LOGIN", "로그인 후 저장할 수 있습니다."),
    WORKSPACE_NO_FILES(HttpStatus.BAD_REQUEST, "WORKSPACE_NO_FILES", "워크스페이스에 업로드된 파일이 없습니다."),
    WORKSPACE_ANALYSIS_SET_NOT_READY(
            HttpStatus.BAD_REQUEST,
            "WORKSPACE_ANALYSIS_SET_NOT_READY",
            "워크스페이스 분석 결과가 아직 준비되지 않았습니다."),
    WORKSPACE_FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "WORKSPACE_FILE_NOT_FOUND", "워크스페이스 파일을 찾을 수 없습니다."),
    WORKSPACE_CONTEXT_NOT_FOUND(HttpStatus.NOT_FOUND, "WORKSPACE_CONTEXT_404", "워크스페이스 컨텍스트를 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}