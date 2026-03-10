package com.sellivu.backend.settlement.controller;

import com.sellivu.backend.settlement.service.SettlementSnapshotRebuildService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/settlement/admin/snapshots")
public class SettlementSnapshotAdminController {

    private final SettlementSnapshotRebuildService settlementSnapshotRebuildService;

    @PostMapping("/rebuild")
    public Map<String, Object> rebuildAll() {
        int rebuiltUploadCount = settlementSnapshotRebuildService.rebuildAll();
        return Map.of(
                "message", "전체 snapshot 재빌드가 완료되었습니다.",
                "rebuiltUploadCount", rebuiltUploadCount
        );
    }

    @PostMapping("/rebuild/upload/{uploadId}")
    public Map<String, Object> rebuildByUpload(@PathVariable Long uploadId) {
        int rebuiltCount = settlementSnapshotRebuildService.rebuildByUpload(uploadId);
        return Map.of(
                "message", "업로드 기준 snapshot 재빌드가 완료되었습니다.",
                "uploadId", uploadId,
                "rebuiltUploadCount", rebuiltCount
        );
    }

    @PostMapping("/rebuild/{snapshotId}")
    public Map<String, Object> rebuildSnapshot(@PathVariable Long snapshotId) {
        settlementSnapshotRebuildService.rebuildSnapshot(snapshotId);
        return Map.of(
                "message", "snapshot 재빌드가 완료되었습니다.",
                "snapshotId", snapshotId
        );
    }

    @GetMapping("/rebuildable-uploads")
    public List<Long> getRebuildableUploadIds() {
        return settlementSnapshotRebuildService.findRebuildableUploadIds();
    }
}