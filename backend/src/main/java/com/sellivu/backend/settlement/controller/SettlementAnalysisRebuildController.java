package com.sellivu.backend.settlement.controller;

import com.sellivu.backend.settlement.dto.SettlementAnalysisRebuildResponse;
import com.sellivu.backend.settlement.service.SettlementAnalysisRebuildService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/settlement/analysis-sets")
public class SettlementAnalysisRebuildController {

    private final SettlementAnalysisRebuildService settlementAnalysisRebuildService;

    @PostMapping("/{analysisSetId}/rebuild")
    public SettlementAnalysisRebuildResponse rebuild(@PathVariable Long analysisSetId) {
        return settlementAnalysisRebuildService.rebuildSet(analysisSetId);
    }
}