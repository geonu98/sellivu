package com.sellivu.backend.analysis.service;

import com.sellivu.backend.analysis.dto.CreateAnalysisJobRequest;
import com.sellivu.backend.analysis.dto.CreateAnalysisJobResponse;
import com.sellivu.backend.analysis.dto.GetAnalysisJobResponse;
import com.sellivu.backend.analysis.entity.AnalysisJob;
import com.sellivu.backend.analysis.repository.AnalysisJobRepository;
import com.sellivu.backend.platform.ProductUrlInfo;
import com.sellivu.backend.platform.ProductUrlParserResolver;
import com.sellivu.backend.product.entity.Product;
import com.sellivu.backend.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AnalysisJobService {

    private final AnalysisJobRepository analysisJobRepository;
    private final ProductUrlParserResolver productUrlParserResolver;
    private final ProductRepository productRepository;

    @Transactional
    public CreateAnalysisJobResponse createJob(CreateAnalysisJobRequest request) {

        ProductUrlInfo urlInfo = productUrlParserResolver.resolve(request.getProductUrl());

        if (!urlInfo.isValid()) {
            throw new IllegalArgumentException("지원하지 않는 상품 상세 URL입니다.");
        }

        Product product = productRepository.findByPlatformAndStoreNameAndExternalProductId(
                        urlInfo.getPlatform(),
                        urlInfo.getStoreName(),
                        urlInfo.getExternalProductId()
                )
                .orElseGet(() -> productRepository.save(Product.create(urlInfo)));

        AnalysisJob analysisJob = AnalysisJob.create(product, urlInfo);
        AnalysisJob saved = analysisJobRepository.save(analysisJob);

        return CreateAnalysisJobResponse.builder()
                .jobId(saved.getId())
                .status(saved.getStatus())
                .message("분석 작업이 생성되었습니다.")
                .build();
    }

    @Transactional(readOnly = true)
    public GetAnalysisJobResponse getJob(Long jobId) {

        AnalysisJob job = analysisJobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("분석 작업을 찾을 수 없습니다."));

        return GetAnalysisJobResponse.builder()
                .jobId(job.getId())
                .requestedUrl(job.getRequestedUrl())
                .normalizedUrl(job.getProduct().getNormalizedUrl())
                .platform(job.getProduct().getPlatform())
                .storeName(job.getProduct().getStoreName())
                .externalProductId(job.getProduct().getExternalProductId())
                .productName(job.getProduct().getProductName())
                .price(job.getProduct().getPrice())
                .rating(job.getProduct().getRating())
                .reviewCount(job.getProduct().getReviewCount())
                .thumbnailUrl(job.getProduct().getThumbnailUrl())
                .lastCrawledAt(job.getProduct().getLastCrawledAt())
                .status(job.getStatus())
                .createdAt(job.getCreatedAt())
                .startedAt(job.getStartedAt())
                .finishedAt(job.getFinishedAt())
                .errorMessage(job.getErrorMessage())
                .build();
    }
}