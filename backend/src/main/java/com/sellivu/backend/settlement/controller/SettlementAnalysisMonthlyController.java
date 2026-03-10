package com.sellivu.backend.settlement.controller;

import com.sellivu.backend.settlement.dto.SettlementMonthlySummaryResponse;
import com.sellivu.backend.settlement.service.SettlementAnalysisMonthlyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/settlement/analysis-sets")
public class SettlementAnalysisMonthlyController {

    private final SettlementAnalysisMonthlyService settlementAnalysisMonthlyService;

    @GetMapping("/{analysisSetId}/monthly")
    public List<SettlementMonthlySummaryResponse> getMonthlySummaries(@PathVariable Long analysisSetId) {
        return settlementAnalysisMonthlyService.getMonthlySummaries(analysisSetId);
    }
}