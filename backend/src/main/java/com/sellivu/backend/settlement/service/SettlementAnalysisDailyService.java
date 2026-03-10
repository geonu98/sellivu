package com.sellivu.backend.settlement.service;

import com.sellivu.backend.settlement.domain.SettlementAnalysisSet;
import com.sellivu.backend.settlement.domain.SettlementAnalysisSetItem;
import com.sellivu.backend.settlement.domain.SettlementFileType;
import com.sellivu.backend.settlement.dto.SettlementDailyRowResponse;
import com.sellivu.backend.settlement.repository.SettlementAnalysisSetItemRepository;
import com.sellivu.backend.settlement.repository.SettlementAnalysisSetRepository;
import com.sellivu.backend.settlement.repository.SettlementDailyRowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SettlementAnalysisDailyService {

    private final SettlementAnalysisSetRepository analysisSetRepository;
    private final SettlementAnalysisSetItemRepository analysisSetItemRepository;
    private final SettlementDailyRowRepository settlementDailyRowRepository;

    public List<SettlementDailyRowResponse> getDailyRows(Long analysisSetId) {
        SettlementAnalysisSet analysisSet = analysisSetRepository.findById(analysisSetId)
                .orElseThrow(() -> new IllegalArgumentException("해당 분석 세트를 찾을 수 없습니다. id=" + analysisSetId));

        List<Long> dailyUploadIds = analysisSetItemRepository.findAllByAnalysisSetIdOrderByIdAsc(analysisSet.getId()).stream()
                .filter(item -> item.getFileType() == SettlementFileType.DAILY_SETTLEMENT)
                .map(SettlementAnalysisSetItem::getUploadId)
                .distinct()
                .toList();

        if (dailyUploadIds.isEmpty()) {
            return Collections.emptyList();
        }

        return settlementDailyRowRepository.findAllByUploadIdInOrderBySettlementCompletedDateAscIdAsc(dailyUploadIds).stream()
                .map(SettlementDailyRowResponse::from)
                .toList();
    }
}