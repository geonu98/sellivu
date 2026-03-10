package com.sellivu.backend.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sellivu.backend.global.error.ApiException;
import com.sellivu.backend.global.error.ErrorCode;
import com.sellivu.backend.global.error.ErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    public static final String AUTH_EXCEPTION_ATTRIBUTE = "AUTH_EXCEPTION";

    private final ObjectMapper objectMapper;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException, ServletException {

        Object exceptionObj = request.getAttribute(AUTH_EXCEPTION_ATTRIBUTE);

        ErrorResponse errorResponse;

        if (exceptionObj instanceof ApiException apiException) {
            errorResponse = ErrorResponse.builder()
                    .timestamp(LocalDateTime.now())
                    .status(apiException.getStatus().value())
                    .error(apiException.getStatus().getReasonPhrase())
                    .code(apiException.getCode())
                    .message(apiException.getMessage())
                    .path(request.getRequestURI())
                    .build();

            response.setStatus(apiException.getStatus().value());
        } else {
            errorResponse = ErrorResponse.builder()
                    .timestamp(LocalDateTime.now())
                    .status(ErrorCode.AUTH_UNAUTHORIZED.getStatus().value())
                    .error(ErrorCode.AUTH_UNAUTHORIZED.getStatus().getReasonPhrase())
                    .code(ErrorCode.AUTH_UNAUTHORIZED.getCode())
                    .message(ErrorCode.AUTH_UNAUTHORIZED.getMessage())
                    .path(request.getRequestURI())
                    .build();

            response.setStatus(ErrorCode.AUTH_UNAUTHORIZED.getStatus().value());
        }

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        objectMapper.writeValue(response.getWriter(), errorResponse);
    }
}