package com.sellivu.backend.product.service;

import com.sellivu.backend.platform.CommercePlatform;
import com.sellivu.backend.product.dto.CrawlProductRequest;
import com.sellivu.backend.product.dto.CrawledProductInfo;
import com.sellivu.backend.product.entity.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmartStoreProductCrawlerService implements ProductCrawlerService {

    private final RestTemplate restTemplate;

    @Override
    public boolean supports(CommercePlatform platform) {
        return platform == CommercePlatform.SMARTSTORE;
    }

    @Override
    public CrawledProductInfo crawl(Product product) {
        log.info("스마트스토어 crawler API 호출 - storeName={}, productId={}",
                product.getStoreName(),
                product.getExternalProductId());

        CrawlProductRequest request = CrawlProductRequest.builder()
                .platform(product.getPlatform())
                .normalizedUrl(product.getNormalizedUrl())
                .storeName(product.getStoreName())
                .externalProductId(product.getExternalProductId())
                .build();

        String url = "http://localhost:3001/internal/crawl/product";

        CrawledProductInfo response =
                restTemplate.postForObject(url, request, CrawledProductInfo.class);

        if (response == null) {
            throw new IllegalStateException("크롤러 응답이 비어 있습니다.");
        }

        return response;
    }
}