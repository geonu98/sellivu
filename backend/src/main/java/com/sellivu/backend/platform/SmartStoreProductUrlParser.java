package com.sellivu.backend.platform;

import org.springframework.stereotype.Component;

import java.net.URI;

@Component
public class SmartStoreProductUrlParser implements ProductUrlParser {

    @Override
    public boolean supports(String url) {
        try {
            URI uri = URI.create(url);
            return "smartstore.naver.com".equals(uri.getHost());
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public ProductUrlInfo parse(String url) {
        try {
            URI uri = URI.create(url);
            String path = uri.getPath(); // /monoform1/products/12403663207
            String[] parts = path.split("/");

            boolean valid = parts.length >= 4
                    && "products".equals(parts[2])
                    && parts[3].matches("\\d+");

            if (!valid) {
                return ProductUrlInfo.builder()
                        .platform(CommercePlatform.SMARTSTORE)
                        .originalUrl(url)
                        .valid(false)
                        .build();
            }

            String storeName = parts[1];
            String externalProductId = parts[3];
            String normalizedUrl = "https://smartstore.naver.com/" + storeName + "/products/" + externalProductId;

            return ProductUrlInfo.builder()
                    .platform(CommercePlatform.SMARTSTORE)
                    .originalUrl(url)
                    .normalizedUrl(normalizedUrl)
                    .storeName(storeName)
                    .externalProductId(externalProductId)
                    .valid(true)
                    .build();

        } catch (Exception e) {
            return ProductUrlInfo.builder()
                    .platform(CommercePlatform.SMARTSTORE)
                    .originalUrl(url)
                    .valid(false)
                    .build();
        }
    }
}