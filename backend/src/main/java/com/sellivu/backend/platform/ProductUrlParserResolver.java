package com.sellivu.backend.platform;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ProductUrlParserResolver {

    private final List<ProductUrlParser> parsers;

    public ProductUrlInfo resolve(String url) {
        return parsers.stream()
                .filter(parser -> parser.supports(url))
                .findFirst()
                .map(parser -> parser.parse(url))
                .orElse(ProductUrlInfo.builder()
                        .platform(CommercePlatform.UNKNOWN)
                        .originalUrl(url)
                        .valid(false)
                        .build());
    }
}