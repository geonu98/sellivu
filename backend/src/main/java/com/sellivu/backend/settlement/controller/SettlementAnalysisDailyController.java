package com.sellivu.backend.settlement.controller;

import com.sellivu.backend.settlement.dto.SettlementDailyRowResponse;
import com.sellivu.backend.settlement.service.SettlementAnalysisDailyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/settlement/analysis-sets")
public class SettlementAnalysisDailyController {

    private final SettlementAnalysisDailyService settlementAnalysisDailyService;

    @GetMapping("/{analysisSetId}/daily")
    public List<SettlementDailyRowResponse> getDailyRows(@PathVariable Long analysisSetId) {
        return settlementAnalysisDailyService.getDailyRows(analysisSetId);
    }
}