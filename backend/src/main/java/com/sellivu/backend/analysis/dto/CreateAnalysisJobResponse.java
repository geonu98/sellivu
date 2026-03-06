package com.sellivu.backend.analysis.dto;

import com.sellivu.backend.analysis.entity.AnalysisJobStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreateAnalysisJobResponse {

    private Long jobId;
    private AnalysisJobStatus status;
    private String message;
}