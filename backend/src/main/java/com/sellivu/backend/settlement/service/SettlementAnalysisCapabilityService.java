package com.sellivu.backend.settlement.service;

import com.sellivu.backend.settlement.domain.AnalysisGapType;
import com.sellivu.backend.settlement.domain.SettlementAnalysisSet;
import com.sellivu.backend.settlement.domain.SettlementAnalysisSetItem;
import com.sellivu.backend.settlement.domain.SettlementFileType;
import com.sellivu.backend.settlement.domain.ViewType;
import com.sellivu.backend.settlement.dto.AnalysisCapabilityResponse;
import com.sellivu.backend.settlement.exception.AnalysisSetNotFoundException;
import com.sellivu.backend.settlement.repository.SettlementAnalysisSetItemRepository;
import com.sellivu.backend.settlement.repository.SettlementAnalysisSetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SettlementAnalysisCapabilityService {

    private final SettlementAnalysisSetRepository analysisSetRepository;
    private final SettlementAnalysisSetItemRepository analysisSetItemRepository;

    public AnalysisCapabilityResponse getCapability(Long analysisSetId) {
        SettlementAnalysisSet set = analysisSetRepository.findById(analysisSetId)
                .orElseThrow(() -> new AnalysisSetNotFoundException(analysisSetId));

        List<SettlementAnalysisSetItem> items =
                analysisSetItemRepository.findAllByAnalysisSetIdOrderByIdAsc(set.getId());

        Set<SettlementFileType> uploadedTypes = EnumSet.noneOf(SettlementFileType.class);
        for (SettlementAnalysisSetItem item : items) {
            if (item.getFileType() != null) {
                uploadedTypes.add(item.getFileType());
            }
        }

        List<SettlementFileType> uploadedFileTypes = uploadedTypes.stream()
                .sorted(Comparator.comparing(Enum::name))
                .toList();

        List<SettlementFileType> missingFileTypes = calculateMissingFileTypes(uploadedTypes);
        List<ViewType> availableViews = calculateAvailableViews(uploadedTypes);
        List<AnalysisGapType> gaps = calculateGaps(uploadedTypes);
        boolean requiresContextOptions = calculateRequiresContextOptions(uploadedTypes);
        List<String> explainablePolicyFactors = calculateExplainablePolicyFactors(uploadedTypes);
        List<String> verificationPendingFields = calculateVerificationPendingFields(uploadedTypes);
        String message = buildMessage(uploadedTypes);

        return new AnalysisCapabilityResponse(
                uploadedFileTypes,
                availableViews,
                missingFileTypes,
                gaps,
                requiresContextOptions,
                explainablePolicyFactors,
                verificationPendingFields,
                message
        );
    }

    private List<SettlementFileType> calculateMissingFileTypes(Set<SettlementFileType> uploadedTypes) {
        List<SettlementFileType> result = new ArrayList<>();

        for (SettlementFileType type : SettlementFileType.values()) {
            if (!uploadedTypes.contains(type)) {
                result.add(type);
            }
        }

        result.sort(Comparator.comparing(Enum::name));
        return result;
    }

    private List<ViewType> calculateAvailableViews(Set<SettlementFileType> uploadedTypes) {
        EnumSet<ViewType> result = EnumSet.noneOf(ViewType.class);

        boolean hasDaily = uploadedTypes.contains(SettlementFileType.DAILY_SETTLEMENT);
        boolean hasOrder = uploadedTypes.contains(SettlementFileType.ORDER_SETTLEMENT);
        boolean hasFee = uploadedTypes.contains(SettlementFileType.FEE_DETAIL);

        if (hasDaily) {
            result.add(ViewType.DAILY);
            result.add(ViewType.MONTHLY);
        }

        if (hasOrder) {
            result.add(ViewType.ORDER_DETAIL);
        }

        if (hasFee) {
            result.add(ViewType.FEE_DETAIL);
        }

        if (hasOrder && hasFee) {
            result.add(ViewType.ORDER_FEE_CROSS_CHECK);
            result.add(ViewType.ISSUES);
            result.add(ViewType.ORDER_DETAIL);
            result.add(ViewType.FEE_DETAIL);
        }

        if (hasDaily && hasOrder) {
            result.add(ViewType.DAILY_CROSS_CHECK);
            result.add(ViewType.ISSUES);
        }

        return result.stream()
                .sorted(Comparator.comparing(Enum::name))
                .toList();
    }

    private List<AnalysisGapType> calculateGaps(Set<SettlementFileType> uploadedTypes) {
        EnumSet<AnalysisGapType> result = EnumSet.noneOf(AnalysisGapType.class);

        boolean hasDaily = uploadedTypes.contains(SettlementFileType.DAILY_SETTLEMENT);
        boolean hasOrder = uploadedTypes.contains(SettlementFileType.ORDER_SETTLEMENT);
        boolean hasFee = uploadedTypes.contains(SettlementFileType.FEE_DETAIL);

        if (!hasDaily) {
            result.add(AnalysisGapType.DAILY_FILE_MISSING);
        }
        if (!hasOrder) {
            result.add(AnalysisGapType.ORDER_FILE_MISSING);
        }
        if (!hasFee) {
            result.add(AnalysisGapType.FEE_FILE_MISSING);
        }

        if (!(hasOrder && hasFee)) {
            result.add(AnalysisGapType.ORDER_FEE_CROSS_CHECK_UNAVAILABLE);
        }

        if (!(hasDaily && hasOrder)) {
            result.add(AnalysisGapType.DAILY_CROSS_CHECK_UNAVAILABLE);
        }

        return result.stream()
                .sorted(Comparator.comparing(Enum::name))
                .toList();
    }

    private boolean calculateRequiresContextOptions(Set<SettlementFileType> uploadedTypes) {
        boolean hasDaily = uploadedTypes.contains(SettlementFileType.DAILY_SETTLEMENT);
        boolean hasOrder = uploadedTypes.contains(SettlementFileType.ORDER_SETTLEMENT);
        boolean hasFee = uploadedTypes.contains(SettlementFileType.FEE_DETAIL);

        return hasDaily || (hasOrder && hasFee);
    }

    private List<String> calculateExplainablePolicyFactors(Set<SettlementFileType> uploadedTypes) {
        List<String> result = new ArrayList<>();

        boolean hasDaily = uploadedTypes.contains(SettlementFileType.DAILY_SETTLEMENT);
        boolean hasOrder = uploadedTypes.contains(SettlementFileType.ORDER_SETTLEMENT);
        boolean hasFee = uploadedTypes.contains(SettlementFileType.FEE_DETAIL);

        if (hasDaily) {
            result.add("혜택정산");
            result.add("일별 공제/환급");
            result.add("마이너스 비즈월렛 상계");
            result.add("반품안심케어 비용");
            result.add("빠른정산");
            result.add("우대수수료 환급");
        }

        if (hasOrder && hasFee) {
            result.add("혜택금액 기반 실수령액 차이");
            result.add("배송비 sectionType 차이");
        }

        return result.stream().distinct().toList();
    }

    private List<String> calculateVerificationPendingFields(Set<SettlementFileType> uploadedTypes) {
        List<String> result = new ArrayList<>();

        boolean hasOrder = uploadedTypes.contains(SettlementFileType.ORDER_SETTLEMENT);
        boolean hasFee = uploadedTypes.contains(SettlementFileType.FEE_DETAIL);

        if (hasOrder) {
            result.add("sectionType 실제 값 검증");
        }

        if (hasFee) {
            result.add("feeType 실제 값 검증");
            result.add("salesLinkedFeeDetail 실제 값 검증");
        }

        return result;
    }

    private String buildMessage(Set<SettlementFileType> uploadedTypes) {
        boolean hasDaily = uploadedTypes.contains(SettlementFileType.DAILY_SETTLEMENT);
        boolean hasOrder = uploadedTypes.contains(SettlementFileType.ORDER_SETTLEMENT);
        boolean hasFee = uploadedTypes.contains(SettlementFileType.FEE_DETAIL);

        if (!hasDaily && !hasOrder && !hasFee) {
            return "이 분석 세트에는 업로드된 정산 파일이 없습니다.";
        }

        if (hasDaily && !hasOrder && !hasFee) {
            return "이 분석 세트에서는 일별 정산 파일만 포함되어 일별/월별 분석만 가능합니다.";
        }

        if (!hasDaily && hasOrder && !hasFee) {
            return "이 분석 세트에서는 건별 정산 파일만 포함되어 건별 정산 조회만 가능합니다.";
        }

        if (!hasDaily && !hasOrder && hasFee) {
            return "이 분석 세트에서는 수수료 상세 파일만 포함되어 수수료 상세 조회만 가능합니다.";
        }

        if (hasDaily && hasOrder && !hasFee) {
            return "이 분석 세트에서는 일별 정산과 건별 정산 파일이 포함되어 일별/월별/건별 분석이 가능하며, 수수료 상세 파일이 없어 수수료 교차검증은 제한됩니다.";
        }

        if (hasDaily && !hasOrder && hasFee) {
            return "이 분석 세트에서는 일별 정산과 수수료 상세 파일이 포함되어 일별/월별 및 수수료 상세 조회가 가능하며, 건별 정산 파일이 없어 건별 교차분석은 제한됩니다.";
        }

        if (!hasDaily && hasOrder && hasFee) {
            return "이 분석 세트에서는 건별 정산과 수수료 상세 파일이 포함되어 건별 교차검증이 가능합니다. 일별 정산 파일이 없어 일별 합계 검증은 제한됩니다.";
        }

        return "이 분석 세트에는 일별 정산, 건별 정산, 수수료 상세 파일이 모두 포함되어 전체 분석과 검증이 가능하며, 정책성 차이에 대한 추가 옵션 입력으로 판정 정확도를 높일 수 있습니다.";
    }
}