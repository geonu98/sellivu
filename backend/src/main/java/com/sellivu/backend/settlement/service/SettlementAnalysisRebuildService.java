package com.sellivu.backend.settlement.service;

import com.sellivu.backend.settlement.domain.SettlementAnalysisSet;
import com.sellivu.backend.settlement.domain.SettlementAnalysisSetItem;
import com.sellivu.backend.settlement.domain.SettlementFileType;
import com.sellivu.backend.settlement.domain.SettlementOrderSnapshot;
import com.sellivu.backend.settlement.dto.SettlementAnalysisRebuildResponse;
import com.sellivu.backend.settlement.exception.AnalysisSetNotFoundException;
import com.sellivu.backend.settlement.repository.SettlementAnalysisSetItemRepository;
import com.sellivu.backend.settlement.repository.SettlementAnalysisSetRepository;
import com.sellivu.backend.settlement.repository.SettlementIssueRepository;
import com.sellivu.backend.settlement.repository.SettlementOrderSnapshotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SettlementAnalysisRebuildService {

    private final SettlementAnalysisSetRepository analysisSetRepository;
    private final SettlementAnalysisSetItemRepository analysisSetItemRepository;
    private final SettlementOrderSnapshotRepository snapshotRepository;
    private final SettlementIssueRepository issueRepository;
    private final SettlementOrderSnapshotService settlementOrderSnapshotService;
    private final SettlementAnalysisIssueService settlementAnalysisIssueService;

    public SettlementAnalysisRebuildResponse rebuildSet(Long analysisSetId) {
        SettlementAnalysisSet analysisSet = analysisSetRepository.findById(analysisSetId)
                .orElseThrow(() -> new AnalysisSetNotFoundException(analysisSetId));

        List<SettlementAnalysisSetItem> items =
                analysisSetItemRepository.findAllByAnalysisSetIdOrderByIdAsc(analysisSet.getId());

        List<Long> orderUploadIds = items.stream()
                .filter(item -> item.getFileType() == SettlementFileType.ORDER_SETTLEMENT)
                .map(SettlementAnalysisSetItem::getUploadId)
                .distinct()
                .toList();

        List<Long> feeUploadIds = items.stream()
                .filter(item -> item.getFileType() == SettlementFileType.FEE_DETAIL)
                .map(SettlementAnalysisSetItem::getUploadId)
                .distinct()
                .toList();

        List<SettlementOrderSnapshot> affectedSnapshots =
                snapshotRepository.findAllByOrderUploadIdInOrFeeUploadIdInOrderByIdDesc(orderUploadIds, feeUploadIds);

        for (SettlementOrderSnapshot snapshot : affectedSnapshots) {
            issueRepository.deleteAllBySnapshotId(snapshot.getId());
        }
        snapshotRepository.deleteAll(affectedSnapshots);

        int rebuiltUploadCount = 0;
        List<Long> rebuiltUploadIds = new ArrayList<>();

        for (SettlementAnalysisSetItem item : items) {
            if (item.getFileType() == SettlementFileType.ORDER_SETTLEMENT
                    || item.getFileType() == SettlementFileType.FEE_DETAIL) {

                if (!rebuiltUploadIds.contains(item.getUploadId())) {
                    settlementOrderSnapshotService.aggregateForUpload(item.getUploadId(), item.getFileType());
                    rebuiltUploadIds.add(item.getUploadId());
                    rebuiltUploadCount++;
                }
            }
        }

        // 핵심 추가:
        // analysis set 기준 issue를 항상 다시 생성
        settlementAnalysisIssueService.rebuildIssues(analysisSet);

        return new SettlementAnalysisRebuildResponse(
                "분석 세트 snapshot 및 issue 재빌드가 완료되었습니다.",
                analysisSetId,
                rebuiltUploadCount,
                affectedSnapshots.size()
        );
    }
}