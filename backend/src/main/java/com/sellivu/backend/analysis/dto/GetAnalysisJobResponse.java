package com.sellivu.backend.analysis.dto;

import com.sellivu.backend.analysis.entity.AnalysisJobStatus;
import com.sellivu.backend.platform.CommercePlatform;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class GetAnalysisJobResponse {

    private Long jobId;
    private String requestedUrl;
    private String normalizedUrl;
    private CommercePlatform platform;
    private String storeName;
    private String externalProductId;
    private AnalysisJobStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private String errorMessage;
}