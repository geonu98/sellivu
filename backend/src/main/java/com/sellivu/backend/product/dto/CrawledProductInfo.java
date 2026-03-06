package com.sellivu.backend.product.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class CrawledProductInfo {

    private String productName;
    private Integer price;
    private BigDecimal rating;
    private Integer reviewCount;
    private String thumbnailUrl;
}