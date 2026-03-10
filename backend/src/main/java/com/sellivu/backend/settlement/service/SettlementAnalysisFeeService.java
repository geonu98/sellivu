package com.sellivu.backend.settlement.service;

import com.sellivu.backend.settlement.domain.SettlementAnalysisSet;
import com.sellivu.backend.settlement.domain.SettlementAnalysisSetItem;
import com.sellivu.backend.settlement.domain.SettlementFileType;
import com.sellivu.backend.settlement.dto.SettlementFeeRowResponse;
import com.sellivu.backend.settlement.repository.SettlementAnalysisSetItemRepository;
import com.sellivu.backend.settlement.repository.SettlementAnalysisSetRepository;
import com.sellivu.backend.settlement.repository.SettlementFeeRowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SettlementAnalysisFeeService {

    private final SettlementAnalysisSetRepository analysisSetRepository;
    private final SettlementAnalysisSetItemRepository analysisSetItemRepository;
    private final SettlementFeeRowRepository settlementFeeRowRepository;

    public List<SettlementFeeRowResponse> getFeeRows(Long analysisSetId) {
        SettlementAnalysisSet analysisSet = analysisSetRepository.findById(analysisSetId)
                .orElseThrow(() -> new IllegalArgumentException("해당 분석 세트를 찾을 수 없습니다. id=" + analysisSetId));

        List<Long> feeUploadIds = analysisSetItemRepository.findAllByAnalysisSetIdOrderByIdAsc(analysisSet.getId()).stream()
                .filter(item -> item.getFileType() == SettlementFileType.FEE_DETAIL)
                .map(SettlementAnalysisSetItem::getUploadId)
                .distinct()
                .toList();

        if (feeUploadIds.isEmpty()) {
            return Collections.emptyList();
        }

        return settlementFeeRowRepository.findAllByUploadIdInOrderBySettlementCompletedDateAscIdAsc(feeUploadIds).stream()
                .map(SettlementFeeRowResponse::from)
                .toList();
    }
}