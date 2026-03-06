package com.sellivu.backend.analysis.controller;

import com.sellivu.backend.analysis.dto.CreateAnalysisJobRequest;
import com.sellivu.backend.analysis.dto.CreateAnalysisJobResponse;
import com.sellivu.backend.analysis.service.AnalysisJobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/analysis-jobs")
@RequiredArgsConstructor
public class AnalysisJobController {

    private final AnalysisJobService analysisJobService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateAnalysisJobResponse create(@Valid @RequestBody CreateAnalysisJobRequest request) {
        return analysisJobService.createJob(request);
    }
}