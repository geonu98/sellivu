package com.sellivu.backend.product.service;

import com.sellivu.backend.platform.CommercePlatform;
import com.sellivu.backend.product.dto.CrawledProductInfo;
import com.sellivu.backend.product.entity.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
public class DummyProductCrawlerService implements ProductCrawlerService {

    @Override
    public boolean supports(CommercePlatform platform) {
        return platform == CommercePlatform.UNKNOWN;
    }

    @Override
    public CrawledProductInfo crawl(Product product) {
        log.info("더미 크롤링 시작 - platform={}, productId={}",
                product.getPlatform(),
                product.getExternalProductId());

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return CrawledProductInfo.builder()
                .productName("더미 상품명")
                .price(10000)
                .rating(new BigDecimal("4.50"))
                .reviewCount(100)
                .thumbnailUrl("https://dummy-image.example.com/default.jpg")
                .build();
    }
}