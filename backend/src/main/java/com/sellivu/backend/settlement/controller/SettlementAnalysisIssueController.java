package com.sellivu.backend.settlement.controller;

import com.sellivu.backend.settlement.dto.SettlementAnalysisIssueResponse;
import com.sellivu.backend.settlement.service.SettlementAnalysisIssueService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/settlement/analysis-sets")
public class SettlementAnalysisIssueController {

    private final SettlementAnalysisIssueService settlementAnalysisIssueService;

    @GetMapping("/{analysisSetId}/issues")
    public List<SettlementAnalysisIssueResponse> getIssues(@PathVariable Long analysisSetId) {
        return settlementAnalysisIssueService.getIssues(analysisSetId);
    }
}