package com.sellivu.backend.product.dto;

import com.sellivu.backend.platform.CommercePlatform;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CrawlProductRequest {

    private CommercePlatform platform;
    private String normalizedUrl;
    private String storeName;
    private String externalProductId;
}