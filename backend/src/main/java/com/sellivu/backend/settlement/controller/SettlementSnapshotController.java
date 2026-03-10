package com.sellivu.backend.settlement.controller;

import com.sellivu.backend.settlement.domain.MatchStatus;
import com.sellivu.backend.settlement.dto.SettlementIssueResponse;
import com.sellivu.backend.settlement.dto.SettlementSnapshotDetailResponse;
import com.sellivu.backend.settlement.dto.SettlementSnapshotSummaryResponse;
import com.sellivu.backend.settlement.service.SettlementSnapshotQueryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/settlement/snapshots")
public class SettlementSnapshotController {

    private final SettlementSnapshotQueryService settlementSnapshotQueryService;

    @GetMapping
    public List<SettlementSnapshotSummaryResponse> getSnapshots(
            @RequestParam(required = false) Long uploadId,
            @RequestParam(required = false) MatchStatus matchStatus
    ) {
        return settlementSnapshotQueryService.getSnapshots(uploadId, matchStatus);
    }

    @GetMapping("/{snapshotId}")
    public SettlementSnapshotDetailResponse getSnapshot(
            @PathVariable Long snapshotId
    ) {
        return settlementSnapshotQueryService.getSnapshot(snapshotId);
    }

    @GetMapping("/{snapshotId}/issues")
    public List<SettlementIssueResponse> getIssues(
            @PathVariable Long snapshotId
    ) {
        return settlementSnapshotQueryService.getIssues(snapshotId);
    }
}