package com.sellivu.backend.settlement.parser;

import com.sellivu.backend.settlement.domain.SettlementFileType;
import com.sellivu.backend.settlement.domain.StandardSettlementField;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SettlementFileDetector {

    public SettlementFileDetectionResult detect(List<String> headers) {
        if (headers == null || headers.isEmpty()) {
            throw new IllegalArgumentException("헤더가 비어 있어 파일 종류를 판별할 수 없습니다.");
        }

        Set<String> normalizedHeaders = headers.stream()
                .map(StandardSettlementField::normalizeHeader)
                .collect(Collectors.toSet());

        if (isDailySettlement(normalizedHeaders)) {
            return new SettlementFileDetectionResult(SettlementFileType.DAILY_SETTLEMENT, headers);
        }

        if (isFeeDetail(normalizedHeaders)) {
            return new SettlementFileDetectionResult(SettlementFileType.FEE_DETAIL, headers);
        }

        if (isOrderSettlement(normalizedHeaders)) {
            return new SettlementFileDetectionResult(SettlementFileType.ORDER_SETTLEMENT, headers);
        }

        throw new IllegalArgumentException("지원하지 않는 정산 파일 형식입니다.");
    }

    private boolean isDailySettlement(Set<String> headers) {
        return containsAll(headers,
                "정산예정일",
                "정산완료일",
                "정산금액",
                "일반정산금액",
                "빠른정산금액",
                "정산기준금액",
                "수수료합계",
                "정산방식"
        );
    }

    private boolean isOrderSettlement(Set<String> headers) {
        return containsAll(headers,
                "주문번호",
                "상품주문번호",
                "결제일",
                "금액변동일",
                "정산기준금액",
                "npay수수료",
                "매출연동수수료합계",
                "정산예정금액"
        );
    }

    private boolean isFeeDetail(Set<String> headers) {
        return containsAll(headers,
                "주문번호",
                "상품주문번호",
                "수수료기준금액",
                "수수료구분",
                "결제수단",
                "매출연동수수료상세",
                "수수료상한액",
                "수수료금액"
        );
    }

    private boolean containsAll(Set<String> actualHeaders, String... requiredHeaders) {
        for (String header : requiredHeaders) {
            String normalized = StandardSettlementField.normalizeHeader(header);
            if (!actualHeaders.contains(normalized)) {
                return false;
            }
        }
        return true;
    }
}