package com.sellivu.backend.settlement.dto;

import com.sellivu.backend.settlement.domain.IssueExplanationCode;
import com.sellivu.backend.settlement.domain.IssueJudgementStatus;
import com.sellivu.backend.settlement.domain.SettlementIssue;
import com.sellivu.backend.settlement.domain.SettlementIssueType;

public final class SettlementIssuePresentationMapper {

    private SettlementIssuePresentationMapper() {
    }

    public static SettlementIssuePresentation from(SettlementIssue issue) {
        String statusLabel = mapStatusLabel(issue.getJudgementStatus());
        boolean explainable = issue.getJudgementStatus() == IssueJudgementStatus.EXPLAINABLE;
        boolean refundCandidate =
                issue.getExplanationCode() == IssueExplanationCode.PREFERRED_FEE_REFUND_POSSIBLE;

        return switch (issue.getIssueType()) {
            case FEE_ROW_UNMATCHED -> new SettlementIssuePresentation(
                    "연결되지 않은 수수료",
                    "수수료가 잡혔지만 연결된 주문을 찾지 못했습니다.",
                    "수수료 상세에는 값이 있지만 주문 상세와 연결되지 않았습니다.",
                    "실제 남는 금액 계산에서 누락이나 중복이 생길 수 있습니다.",
                    "주문번호와 상품주문번호가 원본 파일에 있는지 확인해 주세요.",
                    statusLabel,
                    explainable,
                    refundCandidate
            );

            case ORDER_ROW_UNMATCHED -> new SettlementIssuePresentation(
                    "수수료가 없는 주문",
                    "주문은 있지만 연결된 수수료 정보를 찾지 못했습니다.",
                    "주문 상세에는 값이 있지만 수수료 상세와 연결되지 않았습니다.",
                    "이 주문의 실제 정산액과 순이익 계산이 부정확할 수 있습니다.",
                    "수수료 상세 파일에 해당 주문번호 또는 상품주문번호가 있는지 확인해 주세요.",
                    statusLabel,
                    explainable,
                    refundCandidate
            );

            case SETTLEMENT_AMOUNT_MISMATCH -> new SettlementIssuePresentation(
                    "정산금액 불일치",
                    "주문 정산금액과 수수료 정산금액이 서로 다릅니다.",
                    "두 파일에서 같은 주문의 정산금액이 일치하지 않습니다.",
                    "최종 정산액과 실제 남는 금액 계산에 차이가 생길 수 있습니다.",
                    "원본 주문 상세와 수수료 상세의 정산금액을 비교해 주세요.",
                    statusLabel,
                    explainable,
                    refundCandidate
            );

            case COMMISSION_AMOUNT_MISMATCH -> new SettlementIssuePresentation(
                    "수수료 금액 불일치",
                    "주문 기준 수수료와 수수료 상세 금액이 서로 다릅니다.",
                    "동일 주문에 대해 수수료 값이 다르게 기록되어 있습니다.",
                    "수수료율 검토와 순이익 계산이 부정확할 수 있습니다.",
                    "수수료 상세 항목과 주문 정산 내역의 수수료 값을 비교해 주세요.",
                    statusLabel,
                    explainable,
                    refundCandidate
            );

            case NET_AMOUNT_MISMATCH -> new SettlementIssuePresentation(
                    "실정산금액 불일치",
                    "주문 기준 실정산금액과 수수료 상세 실정산금액이 다릅니다.",
                    "같은 주문인데 실제 정산되는 금액이 파일마다 다릅니다.",
                    "판매자 입금 예상액 계산이 부정확할 수 있습니다.",
                    "실정산금액 차이를 만든 차감 항목이 있는지 확인해 주세요.",
                    statusLabel,
                    explainable,
                    refundCandidate
            );

            case DAILY_ROW_MISSING -> new SettlementIssuePresentation(
                    "일별 정산 데이터 누락",
                    "해당 날짜의 일별 정산 행을 찾지 못했습니다.",
                    "주문/수수료 합계는 있는데 일별 정산 파일에는 해당 날짜 행이 없습니다.",
                    "일별 검증과 날짜별 손익 분석이 불완전할 수 있습니다.",
                    "일별 정산 파일에 해당 날짜 데이터가 포함되어 있는지 확인해 주세요.",
                    statusLabel,
                    explainable,
                    refundCandidate
            );

            case DAILY_ROW_DUPLICATED -> new SettlementIssuePresentation(
                    "일별 정산 행 중복",
                    "해당 날짜의 일별 정산 행이 여러 건 있습니다.",
                    "하나여야 하는 날짜별 정산 행이 중복으로 들어왔습니다.",
                    "일별 정산 합계가 중복 계산될 수 있습니다.",
                    "중복 업로드 또는 중복 행이 있는지 확인해 주세요.",
                    statusLabel,
                    explainable,
                    refundCandidate
            );

            case DAILY_AMOUNT_MISMATCH -> mapDailyAmountMismatch(issue, statusLabel);

            case DUPLICATE_CANDIDATE -> new SettlementIssuePresentation(
                    "중복 가능 항목",
                    "중복으로 집계되었을 가능성이 있는 항목입니다.",
                    "같은 주문 또는 유사한 데이터가 중복 연결되었을 수 있습니다.",
                    "정산금액이나 비용이 실제보다 크게 잡힐 수 있습니다.",
                    "중복 업로드 또는 중복 행 여부를 확인해 주세요.",
                    statusLabel,
                    explainable,
                    refundCandidate
            );

            case MISSING_JOIN_KEY -> new SettlementIssuePresentation(
                    "연결 기준값 누락",
                    "주문과 수수료를 연결할 기준값이 부족합니다.",
                    "join key 생성에 필요한 값이 비어 있거나 불완전합니다.",
                    "매칭 실패로 인해 정산 검증 정확도가 떨어질 수 있습니다.",
                    "주문번호, 상품주문번호, 정산 기준 컬럼을 확인해 주세요.",
                    statusLabel,
                    explainable,
                    refundCandidate
            );
        };
    }

    private static SettlementIssuePresentation mapDailyAmountMismatch(
            SettlementIssue issue,
            String statusLabel
    ) {
        IssueExplanationCode code = issue.getExplanationCode();

        if (code == IssueExplanationCode.PREFERRED_FEE_REFUND_POSSIBLE) {
            return new SettlementIssuePresentation(
                    "일별 정산 합계 차이",
                    "해당 날짜 차이는 우대 수수료 환급 반영 때문일 수 있습니다.",
                    "일별 정산 금액과 주문/수수료 합계가 다르지만, 우대 수수료 환급이 포함되었을 가능성이 있습니다.",
                    "실제 남는 금액과 환급 추적에 영향을 줄 수 있습니다.",
                    "해당 날짜를 환급 후보로 보고 추후 반영 내역과 비교해 주세요.",
                    statusLabel,
                    true,
                    true
            );
        }

        if (code == IssueExplanationCode.SAFE_RETURN_CARE_POSSIBLE) {
            return new SettlementIssuePresentation(
                    "일별 정산 합계 차이",
                    "해당 날짜 차이는 반품안심케어 비용 때문일 수 있습니다.",
                    "일별 정산 금액과 주문/수수료 합계가 다르며 반품안심케어 비용이 포함되었을 수 있습니다.",
                    "순이익 계산에서 실제 남는 돈이 줄어들 수 있습니다.",
                    "반품안심케어 사용 여부와 해당 날짜 비용 반영 여부를 확인해 주세요.",
                    statusLabel,
                    true,
                    false
            );
        }

        if (code == IssueExplanationCode.BENEFIT_SETTLEMENT_POSSIBLE) {
            return new SettlementIssuePresentation(
                    "일별 정산 합계 차이",
                    "해당 날짜 차이는 쿠폰·포인트 혜택 정산 때문일 수 있습니다.",
                    "스토어 쿠폰, 네이버 쿠폰, 포인트 혜택이 일별 정산에 반영되었을 가능성이 있습니다.",
                    "매출은 같아 보여도 실제 정산액과 순이익이 달라질 수 있습니다.",
                    "쿠폰/포인트 사용 여부를 확인해 주세요.",
                    statusLabel,
                    true,
                    false
            );
        }

        if (code == IssueExplanationCode.DAILY_POLICY_ADJUSTMENT_POSSIBLE) {
            return new SettlementIssuePresentation(
                    "일별 정산 합계 차이",
                    "해당 날짜 차이는 환불·차감 조정 때문일 수 있습니다.",
                    "일별 정산에서 정책성 조정 또는 차감 환불이 반영되었을 수 있습니다.",
                    "특정 날짜 순이익이 예상보다 낮거나 높게 보일 수 있습니다.",
                    "클레임/환불 포함 여부를 확인해 주세요.",
                    statusLabel,
                    true,
                    false
            );
        }

        if (code == IssueExplanationCode.BIZ_WALLET_OFFSET_POSSIBLE) {
            return new SettlementIssuePresentation(
                    "일별 정산 합계 차이",
                    "해당 날짜 차이는 비즈월렛 상계 때문일 수 있습니다.",
                    "광고비 또는 비즈월렛 관련 상계 금액이 반영되었을 수 있습니다.",
                    "정산 예정액이 실제보다 줄어든 것처럼 보일 수 있습니다.",
                    "비즈월렛 상계 사용 여부를 확인해 주세요.",
                    statusLabel,
                    true,
                    false
            );
        }

        if (code == IssueExplanationCode.FAST_SETTLEMENT_POSSIBLE) {
            return new SettlementIssuePresentation(
                    "일별 정산 합계 차이",
                    "해당 날짜 차이는 빠른정산 비용 또는 반영 시점 차이일 수 있습니다.",
                    "빠른정산 관련 금액이 포함되었을 가능성이 있습니다.",
                    "입금 예상액과 실제 남는 금액 계산이 달라질 수 있습니다.",
                    "빠른정산 사용 여부를 확인해 주세요.",
                    statusLabel,
                    true,
                    false
            );
        }

        if (code == IssueExplanationCode.SETTLEMENT_METHOD_REVIEW_REQUIRED) {
            return new SettlementIssuePresentation(
                    "일별 정산 합계 차이",
                    "정산 방식 확인이 필요한 차이입니다.",
                    "정산 방식 또는 반영 기준 차이 때문에 금액이 달라졌을 수 있습니다.",
                    "일별 검증 결과를 바로 확정하기 어렵습니다.",
                    "정산 방식과 원본 파일 기준을 다시 확인해 주세요.",
                    statusLabel,
                    false,
                    false
            );
        }

        return new SettlementIssuePresentation(
                "일별 정산 합계 차이",
                "해당 날짜의 정산 합계가 비교 파일과 다릅니다.",
                "일별 정산 금액과 주문/수수료 합계가 일치하지 않습니다.",
                "날짜별 손익과 정산 검증 결과가 부정확할 수 있습니다.",
                "분석 옵션과 원본 파일 포함 범위를 다시 확인해 주세요.",
                statusLabel,
                false,
                false
        );
    }

    private static String mapStatusLabel(IssueJudgementStatus status) {
        if (status == null) {
            return "확인 필요";
        }

        return switch (status) {
            case CONFIRMED -> "확인 필요";
            case EXPLAINABLE -> "설명 가능";
            case PENDING -> "확인 필요";
        };
    }
}