package com.sellivu.backend.product.entity;

import com.sellivu.backend.platform.CommercePlatform;
import com.sellivu.backend.platform.ProductUrlInfo;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "platform", nullable = false, length = 30)
    private CommercePlatform platform;

    @Column(name = "store_name", length = 255)
    private String storeName;

    @Column(name = "external_product_id", nullable = false, length = 100)
    private String externalProductId;

    @Column(name = "original_url", nullable = false, columnDefinition = "TEXT")
    private String originalUrl;

    @Column(name = "normalized_url", nullable = false, columnDefinition = "TEXT")
    private String normalizedUrl;

    @Column(name = "product_name", length = 500)
    private String productName;

    @Column(name = "price")
    private Integer price;

    @Column(name = "rating", precision = 3, scale = 2)
    private BigDecimal rating;

    @Column(name = "review_count")
    private Integer reviewCount;

    @Column(name = "thumbnail_url", columnDefinition = "TEXT")
    private String thumbnailUrl;

    @Column(name = "last_crawled_at")
    private LocalDateTime lastCrawledAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Builder
    private Product(
            CommercePlatform platform,
            String storeName,
            String externalProductId,
            String originalUrl,
            String normalizedUrl,
            String productName,
            Integer price,
            BigDecimal rating,
            Integer reviewCount,
            String thumbnailUrl,
            LocalDateTime lastCrawledAt,
            LocalDateTime createdAt
    ) {
        this.platform = platform;
        this.storeName = storeName;
        this.externalProductId = externalProductId;
        this.originalUrl = originalUrl;
        this.normalizedUrl = normalizedUrl;
        this.productName = productName;
        this.price = price;
        this.rating = rating;
        this.reviewCount = reviewCount;
        this.thumbnailUrl = thumbnailUrl;
        this.lastCrawledAt = lastCrawledAt;
        this.createdAt = createdAt;
    }

    public static Product create(ProductUrlInfo urlInfo) {
        return Product.builder()
                .platform(urlInfo.getPlatform())
                .storeName(urlInfo.getStoreName())
                .externalProductId(urlInfo.getExternalProductId())
                .originalUrl(urlInfo.getOriginalUrl())
                .normalizedUrl(urlInfo.getNormalizedUrl())
                .createdAt(LocalDateTime.now())
                .build();
    }

    public void updateProductInfo(
            String productName,
            Integer price,
            BigDecimal rating,
            Integer reviewCount,
            String thumbnailUrl
    ) {
        this.productName = productName;
        this.price = price;
        this.rating = rating;
        this.reviewCount = reviewCount;
        this.thumbnailUrl = thumbnailUrl;
        this.lastCrawledAt = LocalDateTime.now();
    }
}