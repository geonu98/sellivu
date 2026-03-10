package com.sellivu.backend.settlement.controller;

import com.sellivu.backend.settlement.dto.SettlementOrderRowResponse;
import com.sellivu.backend.settlement.service.SettlementAnalysisOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/settlement/analysis-sets")
public class SettlementAnalysisOrderController {

    private final SettlementAnalysisOrderService settlementAnalysisOrderService;

    @GetMapping("/{analysisSetId}/orders")
    public List<SettlementOrderRowResponse> getOrderRows(@PathVariable Long analysisSetId) {
        return settlementAnalysisOrderService.getOrderRows(analysisSetId);
    }
}