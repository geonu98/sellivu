package com.sellivu.backend.analysis.service;

import com.sellivu.backend.analysis.dto.CreateAnalysisJobRequest;
import com.sellivu.backend.analysis.dto.CreateAnalysisJobResponse;
import com.sellivu.backend.analysis.entity.AnalysisJob;
import com.sellivu.backend.analysis.repository.AnalysisJobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AnalysisJobService {

    private final AnalysisJobRepository analysisJobRepository;

    @Transactional
    public CreateAnalysisJobResponse createJob(CreateAnalysisJobRequest request) {
        AnalysisJob analysisJob = AnalysisJob.create(request.getProductUrl());
        AnalysisJob saved = analysisJobRepository.save(analysisJob);

        return CreateAnalysisJobResponse.builder()
                .jobId(saved.getId())
                .status(saved.getStatus())
                .message("분석 작업이 생성되었습니다.")
                .build();
    }
}