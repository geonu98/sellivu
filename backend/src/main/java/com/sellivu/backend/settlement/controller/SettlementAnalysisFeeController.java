package com.sellivu.backend.settlement.controller;

import com.sellivu.backend.settlement.dto.SettlementFeeRowResponse;
import com.sellivu.backend.settlement.service.SettlementAnalysisFeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/settlement/analysis-sets")
public class SettlementAnalysisFeeController {

    private final SettlementAnalysisFeeService settlementAnalysisFeeService;

    @GetMapping("/{analysisSetId}/fees")
    public List<SettlementFeeRowResponse> getFeeRows(@PathVariable Long analysisSetId) {
        return settlementAnalysisFeeService.getFeeRows(analysisSetId);
    }
}