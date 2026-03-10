package com.sellivu.backend.settlement.service;

import com.sellivu.backend.settlement.domain.SettlementAnalysisSet;
import com.sellivu.backend.settlement.domain.SettlementAnalysisSetItem;
import com.sellivu.backend.settlement.domain.SettlementFileType;
import com.sellivu.backend.settlement.dto.SettlementOrderRowResponse;
import com.sellivu.backend.settlement.repository.SettlementAnalysisSetItemRepository;
import com.sellivu.backend.settlement.repository.SettlementAnalysisSetRepository;
import com.sellivu.backend.settlement.repository.SettlementOrderRowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SettlementAnalysisOrderService {

    private final SettlementAnalysisSetRepository analysisSetRepository;
    private final SettlementAnalysisSetItemRepository analysisSetItemRepository;
    private final SettlementOrderRowRepository settlementOrderRowRepository;

    public List<SettlementOrderRowResponse> getOrderRows(Long analysisSetId) {
        SettlementAnalysisSet analysisSet = analysisSetRepository.findById(analysisSetId)
                .orElseThrow(() -> new IllegalArgumentException("해당 분석 세트를 찾을 수 없습니다. id=" + analysisSetId));

        List<Long> orderUploadIds = analysisSetItemRepository.findAllByAnalysisSetIdOrderByIdAsc(analysisSet.getId()).stream()
                .filter(item -> item.getFileType() == SettlementFileType.ORDER_SETTLEMENT)
                .map(SettlementAnalysisSetItem::getUploadId)
                .distinct()
                .toList();

        if (orderUploadIds.isEmpty()) {
            return Collections.emptyList();
        }

        return settlementOrderRowRepository.findAllByUploadIdInOrderBySettlementCompletedDateAscIdAsc(orderUploadIds).stream()
                .map(SettlementOrderRowResponse::from)
                .toList();
    }
}