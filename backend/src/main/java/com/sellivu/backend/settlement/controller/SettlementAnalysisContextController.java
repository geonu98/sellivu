package com.sellivu.backend.settlement.controller;

import com.sellivu.backend.settlement.dto.SettlementAnalysisContextResponse;
import com.sellivu.backend.settlement.dto.UpdateAnalysisContextRequest;
import com.sellivu.backend.settlement.service.SettlementAnalysisContextService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/settlement/analysis-sets")
public class SettlementAnalysisContextController {

    private final SettlementAnalysisContextService settlementAnalysisContextService;

    @GetMapping("/{analysisSetId}/context")
    public SettlementAnalysisContextResponse getContext(@PathVariable Long analysisSetId) {
        return settlementAnalysisContextService.getContext(analysisSetId);
    }

    @PutMapping("/{analysisSetId}/context")
    public SettlementAnalysisContextResponse updateContext(
            @PathVariable Long analysisSetId,
            @RequestBody UpdateAnalysisContextRequest request
    ) {
        return settlementAnalysisContextService.upsertContext(analysisSetId, request);
    }
}