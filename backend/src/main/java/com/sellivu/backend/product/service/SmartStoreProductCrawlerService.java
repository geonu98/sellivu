package com.sellivu.backend.product.service;

import com.sellivu.backend.platform.CommercePlatform;
import com.sellivu.backend.product.dto.CrawledProductInfo;
import com.sellivu.backend.product.entity.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
public class SmartStoreProductCrawlerService implements ProductCrawlerService {

    @Override
    public boolean supports(CommercePlatform platform) {
        return platform == CommercePlatform.SMARTSTORE;
    }

    @Override
    public CrawledProductInfo crawl(Product product) {
        log.info("스마트스토어 크롤링 시작 - storeName={}, productId={}, url={}",
                product.getStoreName(),
                product.getExternalProductId(),
                product.getNormalizedUrl());

        // TODO:
        // 1. 실제 스마트스토어 접근
        // 2. HTML 파싱 또는 브라우저 자동화
        // 3. 상품명 / 가격 / 썸네일 추출

        return CrawledProductInfo.builder()
                .productName("[스마트스토어] 테스트 상품")
                .price(19900)
                .rating(new BigDecimal("4.78"))
                .reviewCount(1523)
                .thumbnailUrl("https://dummy-image.example.com/smartstore-product.jpg")
                .build();
    }
}