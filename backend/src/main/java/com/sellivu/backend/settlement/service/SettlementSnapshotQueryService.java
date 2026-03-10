package com.sellivu.backend.settlement.service;

import com.sellivu.backend.settlement.domain.MatchStatus;

import com.sellivu.backend.settlement.domain.SettlementIssue;
import com.sellivu.backend.settlement.domain.SettlementOrderSnapshot;
import com.sellivu.backend.settlement.dto.SettlementIssueResponse;
import com.sellivu.backend.settlement.dto.SettlementSnapshotDetailResponse;
import com.sellivu.backend.settlement.dto.SettlementSnapshotSummaryResponse;

import com.sellivu.backend.settlement.exception.SettlementSnapshotNotFoundException;

import com.sellivu.backend.settlement.repository.SettlementIssueRepository;
import com.sellivu.backend.settlement.repository.SettlementOrderSnapshotRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SettlementSnapshotQueryService {

    private final SettlementOrderSnapshotRepository snapshotRepository;
    private final SettlementIssueRepository issueRepository;


    @Transactional(readOnly = true)
    public List<SettlementSnapshotSummaryResponse> getSnapshots(Long uploadId, MatchStatus matchStatus) {
        List<SettlementOrderSnapshot> snapshots;

        if (uploadId != null) {
            snapshots = snapshotRepository.findAllByOrderUploadIdOrFeeUploadIdOrderByIdDesc(uploadId, uploadId);
        } else if (matchStatus != null) {
            snapshots = snapshotRepository.findAllByMatchStatusOrderByIdDesc(matchStatus);
        } else {
            snapshots = snapshotRepository.findAllByOrderByIdDesc();
        }

        return snapshots.stream()
                .map(SettlementSnapshotSummaryResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public SettlementSnapshotDetailResponse getSnapshot(Long snapshotId) {
        SettlementOrderSnapshot snapshot = snapshotRepository.findById(snapshotId)
                .orElseThrow(() -> new SettlementSnapshotNotFoundException(snapshotId));

        List<SettlementIssueResponse> issues = issueRepository.findAllBySnapshotIdOrderByIdAsc(snapshotId).stream()
                .map(SettlementIssueResponse::from)
                .toList();

        return SettlementSnapshotDetailResponse.from(snapshot, issues);
    }

    @Transactional(readOnly = true)
    public List<SettlementIssueResponse> getIssues(Long snapshotId) {
        snapshotRepository.findById(snapshotId)
                .orElseThrow(() -> new SettlementSnapshotNotFoundException(snapshotId));

        List<SettlementIssue> issues = issueRepository.findAllBySnapshotIdOrderByIdAsc(snapshotId);
        return issues.stream()
                .map(SettlementIssueResponse::from)
                .toList();
}
}