package com.sellivu.backend.settlement.controller;

import com.sellivu.backend.settlement.dto.CreateAnalysisSetRequest;
import com.sellivu.backend.settlement.dto.SettlementAnalysisSetItemResponse;
import com.sellivu.backend.settlement.dto.SettlementAnalysisSetResponse;
import com.sellivu.backend.settlement.dto.WorkspaceContextResponse;
import com.sellivu.backend.settlement.service.SettlementAnalysisSetService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/settlement/analysis-sets")
public class SettlementAnalysisSetController {

    private final SettlementAnalysisSetService settlementAnalysisSetService;

    @PostMapping
    public SettlementAnalysisSetResponse createSet(@RequestBody(required = false) CreateAnalysisSetRequest request) {
        String name = request == null ? null : request.getName();
        return settlementAnalysisSetService.createSet(name);
    }

    @GetMapping
    public List<SettlementAnalysisSetResponse> getSets() {
        return settlementAnalysisSetService.getSets();
    }

    @PostMapping("/{analysisSetId}/uploads/{uploadId}")
    public SettlementAnalysisSetItemResponse addUploadToSet(
            @PathVariable Long analysisSetId,
            @PathVariable Long uploadId
    ) {
        return settlementAnalysisSetService.addUploadToSet(analysisSetId, uploadId);
    }

    @GetMapping("/{analysisSetId}/items")
    public List<SettlementAnalysisSetItemResponse> getSetItems(@PathVariable Long analysisSetId) {
        return settlementAnalysisSetService.getSetItems(analysisSetId);
    }

    @GetMapping("/my")
    public List<SettlementAnalysisSetResponse> getMySavedSets() {
        return settlementAnalysisSetService.getMySavedSets(null);
    }

    @GetMapping("/my/{analysisSetId}")
    public SettlementAnalysisSetResponse getMySavedSet(
            @PathVariable Long analysisSetId
    ) {
        return settlementAnalysisSetService.getMySavedSet(analysisSetId, null);
    }

    @GetMapping("/my/{analysisSetId}/items")
    public List<SettlementAnalysisSetItemResponse> getMySavedSetItems(
            @PathVariable Long analysisSetId
    ) {
        settlementAnalysisSetService.validateMySavedSetAccessible(analysisSetId, null);
        return settlementAnalysisSetService.getSetItems(analysisSetId);
    }

    @GetMapping("/my/{analysisSetId}/context")
    public WorkspaceContextResponse getMySavedSetContext(
            @PathVariable Long analysisSetId
    ) {
        return settlementAnalysisSetService.getMySavedSetContext(analysisSetId, null);
    }
}