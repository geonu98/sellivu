package com.sellivu.backend.product.service;

import com.sellivu.backend.product.entity.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DummyProductCrawlerService implements ProductCrawlerService {

    @Override
    public void crawl(Product product) {
        log.info("상품 크롤링 시작 - platform={}, storeName={}, productId={}",
                product.getPlatform(),
                product.getStoreName(),
                product.getExternalProductId());

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        log.info("상품 크롤링 완료 - normalizedUrl={}", product.getNormalizedUrl());
    }
}