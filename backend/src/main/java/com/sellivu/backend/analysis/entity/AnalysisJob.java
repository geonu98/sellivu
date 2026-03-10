package com.sellivu.backend.analysis.entity;

import com.sellivu.backend.platform.ProductUrlInfo;
import com.sellivu.backend.product.entity.Product;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "analysis_jobs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AnalysisJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "requested_url", nullable = false, columnDefinition = "TEXT")
    private String requestedUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AnalysisJobStatus status;

    @Column(name = "error_code", length = 100)
    private String errorCode;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Builder
    private AnalysisJob(
            Product product,
            String requestedUrl,
            AnalysisJobStatus status,
            String errorCode,
            String errorMessage,
            LocalDateTime startedAt,
            LocalDateTime finishedAt,
            LocalDateTime createdAt
    ) {
        this.product = product;
        this.requestedUrl = requestedUrl;
        this.status = status;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
        this.createdAt = createdAt;
    }

    public static AnalysisJob create(Product product, ProductUrlInfo urlInfo) {
        return AnalysisJob.builder()
                .product(product)
                .requestedUrl(urlInfo.getOriginalUrl())
                .status(AnalysisJobStatus.QUEUED)
                .createdAt(LocalDateTime.now())
                .build();
    }
    public void startCrawling() {
        this.status = AnalysisJobStatus.CRAWLING;
        this.startedAt = LocalDateTime.now();
        this.finishedAt = null;
        this.errorCode = null;
        this.errorMessage = null;
    }

    public void complete() {
        this.status = AnalysisJobStatus.COMPLETED;
        this.finishedAt = LocalDateTime.now();
        this.errorCode = null;
        this.errorMessage = null;
    }

    public void fail(String errorCode, String errorMessage) {
        this.status = AnalysisJobStatus.FAILED;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.finishedAt = LocalDateTime.now();
    }
}