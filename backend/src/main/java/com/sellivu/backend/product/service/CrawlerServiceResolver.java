package com.sellivu.backend.product.service;

import com.sellivu.backend.platform.CommercePlatform;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CrawlerServiceResolver {

    private final List<ProductCrawlerService> crawlerServices;

    public ProductCrawlerService resolve(CommercePlatform platform) {
        return crawlerServices.stream()
                .filter(service -> service.supports(platform))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 플랫폼 크롤러입니다."));
    }
}