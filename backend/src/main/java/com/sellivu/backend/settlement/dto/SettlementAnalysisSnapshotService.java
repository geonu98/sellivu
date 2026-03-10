package com.sellivu.backend.settlement.service;

import com.sellivu.backend.settlement.domain.SettlementAnalysisSet;
import com.sellivu.backend.settlement.domain.SettlementAnalysisSetItem;
import com.sellivu.backend.settlement.domain.SettlementFileType;
import com.sellivu.backend.settlement.dto.SettlementSnapshotResponse;
import com.sellivu.backend.settlement.exception.AnalysisSetNotFoundException;
import com.sellivu.backend.settlement.repository.SettlementAnalysisSetItemRepository;
import com.sellivu.backend.settlement.repository.SettlementAnalysisSetRepository;
import com.sellivu.backend.settlement.repository.SettlementOrderSnapshotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SettlementAnalysisSnapshotService {

    private final SettlementAnalysisSetRepository analysisSetRepository;
    private final SettlementAnalysisSetItemRepository analysisSetItemRepository;
    private final SettlementOrderSnapshotRepository snapshotRepository;

    public List<SettlementSnapshotResponse> getSnapshots(Long analysisSetId) {
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

        if (orderUploadIds.isEmpty() && feeUploadIds.isEmpty()) {
            return Collections.emptyList();
        }

        return snapshotRepository
                .findAllByOrderUploadIdInOrFeeUploadIdInOrderByIdDesc(orderUploadIds, feeUploadIds)
                .stream()
                .map(SettlementSnapshotResponse::from)
                .toList();
    }
}