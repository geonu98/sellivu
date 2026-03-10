package com.sellivu.backend.settlement.service;

import com.sellivu.backend.settlement.domain.AnalysisOptionValue;
import com.sellivu.backend.settlement.domain.SettlementAnalysisContext;
import com.sellivu.backend.settlement.dto.SettlementAnalysisContextResponse;
import com.sellivu.backend.settlement.dto.UpdateAnalysisContextRequest;
import com.sellivu.backend.settlement.exception.AnalysisSetNotFoundException;
import com.sellivu.backend.settlement.exception.InvalidAnalysisContextOptionException;
import com.sellivu.backend.settlement.repository.SettlementAnalysisContextRepository;
import com.sellivu.backend.settlement.repository.SettlementAnalysisSetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SettlementAnalysisContextService {

    private final SettlementAnalysisSetRepository analysisSetRepository;
    private final SettlementAnalysisContextRepository analysisContextRepository;

    @Transactional(readOnly = true)
    public SettlementAnalysisContextResponse getContext(Long analysisSetId) {
        ensureAnalysisSetExists(analysisSetId);

        SettlementAnalysisContext context = analysisContextRepository.findByAnalysisSetId(analysisSetId)
                .orElseThrow(() -> new IllegalStateException(
                        "분석 컨텍스트가 존재하지 않습니다. analysisSetId=" + analysisSetId
                ));

        return SettlementAnalysisContextResponse.from(context);
    }

    public SettlementAnalysisContextResponse upsertContext(
            Long analysisSetId,
            UpdateAnalysisContextRequest request
    ) {
        ensureAnalysisSetExists(analysisSetId);

        SettlementAnalysisContext context = analysisContextRepository.findByAnalysisSetId(analysisSetId)
                .orElseGet(() -> SettlementAnalysisContext.createDefault(analysisSetId));

        context.update(
                parse("storeCouponUsage", request.getStoreCouponUsage()),
                parse("naverCouponUsage", request.getNaverCouponUsage()),
                parse("pointBenefitUsage", request.getPointBenefitUsage()),
                parse("safeReturnCareUsage", request.getSafeReturnCareUsage()),
                parse("bizWalletOffsetUsage", request.getBizWalletOffsetUsage()),
                parse("fastSettlementUsage", request.getFastSettlementUsage()),
                parse("claimIncluded", request.getClaimIncluded())
        );

        SettlementAnalysisContext saved = analysisContextRepository.save(context);
        return SettlementAnalysisContextResponse.from(saved);
    }

    private void ensureAnalysisSetExists(Long analysisSetId) {
        analysisSetRepository.findById(analysisSetId)
                .orElseThrow(() -> new AnalysisSetNotFoundException(analysisSetId));
    }

    private AnalysisOptionValue parse(String field, String value) {
        if (value == null || value.isBlank()) {
            return AnalysisOptionValue.UNKNOWN;
        }

        try {
            return AnalysisOptionValue.valueOf(value.trim().toUpperCase());
        } catch (Exception e) {
            throw new InvalidAnalysisContextOptionException(field, value);
        }
    }
}