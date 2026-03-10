package com.sellivu.backend.settlement.controller;

import com.sellivu.backend.settlement.dto.SettlementIssueResponse;
import com.sellivu.backend.settlement.service.SettlementIssueService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/settlement/issues")
public class SettlementIssueController {

    private final SettlementIssueService settlementIssueService;

    @GetMapping
    public List<SettlementIssueResponse> getAllIssues() {
        return settlementIssueService.getAllIssues();
    }

    @GetMapping("/daily")
    public List<SettlementIssueResponse> getDailyIssues() {
        return settlementIssueService.getDailyIssues();
    }

    @GetMapping("/snapshot/{snapshotId}")
    public List<SettlementIssueResponse> getIssuesBySnapshotId(@PathVariable Long snapshotId) {
        return settlementIssueService.getIssuesBySnapshotId(snapshotId);
    }
}