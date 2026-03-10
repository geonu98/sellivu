package com.sellivu.backend.settlement.controller;

import com.sellivu.backend.settlement.dto.AnalysisCapabilityResponse;
import com.sellivu.backend.settlement.service.SettlementAnalysisCapabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/settlement/analysis-capability")
public class SettlementAnalysisCapabilityController {

    private final SettlementAnalysisCapabilityService settlementAnalysisCapabilityService;

    @GetMapping("/{analysisSetId}")
    public AnalysisCapabilityResponse getCapability(@PathVariable Long analysisSetId) {
        return settlementAnalysisCapabilityService.getCapability(analysisSetId);
    }
}