package com.sellivu.backend.global.security;

import com.sellivu.backend.global.error.ApiException;
import com.sellivu.backend.global.error.ErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        return path.startsWith("/api/auth/")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.equals("/favicon.ico");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            String token = resolveToken(request);

            if (!StringUtils.hasText(token)) {
                filterChain.doFilter(request, response);
                return;
            }

            if (!jwtTokenProvider.isAccessToken(token)) {
                throw new ApiException(ErrorCode.AUTH_INVALID_TOKEN, "Access Token 이 아닙니다.");
            }

            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                Long userId = jwtTokenProvider.getUserId(token);
                String email = jwtTokenProvider.getEmail(token);
                String name = jwtTokenProvider.getName(token);
                String role = jwtTokenProvider.getRole(token);

                CustomUserPrincipal principal = new CustomUserPrincipal(
                        userId,
                        email,
                        "",
                        name,
                        role
                );

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                principal,
                                null,
                                principal.getAuthorities()
                        );

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            filterChain.doFilter(request, response);

        } catch (ApiException e) {
            SecurityContextHolder.clearContext();
            request.setAttribute(CustomAuthenticationEntryPoint.AUTH_EXCEPTION_ATTRIBUTE, e);
            customAuthenticationEntryPoint.commence(
                    request,
                    response,
                    new InsufficientAuthenticationException(e.getMessage(), e)
            );
        }
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (!StringUtils.hasText(bearerToken)) {
            return null;
        }

        if (!bearerToken.startsWith("Bearer ")) {
            throw new ApiException(ErrorCode.AUTH_INVALID_TOKEN, "Bearer 토큰 형식이 아닙니다.");
        }

        String token = bearerToken.substring(7).trim();

        if (!StringUtils.hasText(token)) {
            throw new ApiException(ErrorCode.AUTH_MISSING_TOKEN);
        }

        return token;
    }
}