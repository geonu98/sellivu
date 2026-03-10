package com.sellivu.backend.settlement.controller;

import com.sellivu.backend.settlement.dto.SettlementSnapshotResponse;
import com.sellivu.backend.settlement.service.SettlementAnalysisSnapshotService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/settlement/analysis-sets")
public class SettlementAnalysisSnapshotController {

    private final SettlementAnalysisSnapshotService settlementAnalysisSnapshotService;

    @GetMapping("/{analysisSetId}/snapshots")
    public List<SettlementSnapshotResponse> getSnapshots(@PathVariable Long analysisSetId) {
        return settlementAnalysisSnapshotService.getSnapshots(analysisSetId);
    }
}