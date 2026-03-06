package com.sellivu.backend.platform;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductUrlInfo {
    private CommercePlatform platform;
    private String originalUrl;
    private String normalizedUrl;
    private String storeName;
    private String externalProductId;
    private boolean valid;
}