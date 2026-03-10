package com.sellivu.backend.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.auth.cookie")
public record AuthCookieProperties(
        boolean secure,
        String sameSite,
        String domain,
        long maxAgeSeconds
) {
}