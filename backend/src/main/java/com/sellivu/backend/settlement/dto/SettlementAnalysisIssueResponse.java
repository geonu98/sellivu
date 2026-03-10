package com.sellivu.backend.settlement.dto;

import com.sellivu.backend.settlement.domain.IssueExplanationCode;
import com.sellivu.backend.settlement.domain.SettlementIssue;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class SettlementAnalysisIssueResponse {

    private final Long id;
    private final String sourceType;
    private final Long snapshotId;
    private final String issueType;
    private final String orderNo;
    private final String productOrderNo;
    private final String joinKey;
    private final String message;
    private final boolean resolved;
    private final String severity;
    private final String judgementStatus;
    private final String explanationCode;
    private final boolean needsUserInput;
    private final String possibleReasonMessage;
    private final LocalDate issueDate;
    private final LocalDateTime createdAt;

    public SettlementAnalysisIssueResponse(
            Long id,
            String sourceType,
            Long snapshotId,
            String issueType,
            String orderNo,
            String productOrderNo,
            String joinKey,
            String message,
            boolean resolved,
            String severity,
            String judgementStatus,
            String explanationCode,
            boolean needsUserInput,
            String possibleReasonMessage,
            LocalDate issueDate,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.sourceType = sourceType;
        this.snapshotId = snapshotId;
        this.issueType = issueType;
        this.orderNo = orderNo;
        this.productOrderNo = productOrderNo;
        this.joinKey = joinKey;
        this.message = message;
        this.resolved = resolved;
        this.severity = severity;
        this.judgementStatus = judgementStatus;
        this.explanationCode = explanationCode;
        this.needsUserInput = needsUserInput;
        this.possibleReasonMessage = possibleReasonMessage;
        this.issueDate = issueDate;
        this.createdAt = createdAt;
    }

    public static SettlementAnalysisIssueResponse fromEntity(SettlementIssue issue) {
        return new SettlementAnalysisIssueResponse(
                issue.getId(),
                "SNAPSHOT",
                issue.getSnapshotId(),
                issue.getIssueType().name(),
                issue.getOrderNo(),
                issue.getProductOrderNo(),
                issue.getJoinKey(),
                issue.getMessage(),
                issue.isResolved(),
                issue.getSeverity() != null ? issue.getSeverity().name() : null,
                issue.getJudgementStatus() != null ? issue.getJudgementStatus().name() : null,
                issue.getExplanationCode() != null ? issue.getExplanationCode().name() : null,
                issue.isNeedsUserInput(),
                buildPossibleReasonMessage(issue.getExplanationCode()),
                null,
                issue.getCreatedAt()
        );
    }

    public static SettlementAnalysisIssueResponse fromDailyEntity(
            SettlementIssue issue,
            LocalDate issueDate
    ) {
        return new SettlementAnalysisIssueResponse(
                issue.getId(),
                "DAILY_CROSS_CHECK",
                null,
                issue.getIssueType().name(),
                null,
                null,
                issue.getJoinKey(),
                issue.getMessage(),
                issue.isResolved(),
                issue.getSeverity() != null ? issue.getSeverity().name() : null,
                issue.getJudgementStatus() != null ? issue.getJudgementStatus().name() : null,
                issue.getExplanationCode() != null ? issue.getExplanationCode().name() : null,
                issue.isNeedsUserInput(),
                buildPossibleReasonMessage(issue.getExplanationCode()),
                issueDate,
                issue.getCreatedAt()
        );
    }

    private static String buildPossibleReasonMessage(IssueExplanationCode code) {
        if (code == null) {
            return null;
        }

        return switch (code) {
            case BENEFIT_SETTLEMENT_POSSIBLE -> "혜택정산 항목이 포함되어 일별 합계와 차이가 날 수 있습니다.";
            case DAILY_POLICY_ADJUSTMENT_POSSIBLE -> "일별 공제/환급 같은 사후 조정 항목 때문에 차이가 날 수 있습니다.";
            case BIZ_WALLET_OFFSET_POSSIBLE -> "마이너스 비즈월렛 상계가 반영되어 차이가 날 수 있습니다.";
            case SAFE_RETURN_CARE_POSSIBLE -> "반품안심케어 비용 차감이 반영되어 차이가 날 수 있습니다.";
            case FAST_SETTLEMENT_POSSIBLE -> "빠른정산 금액 반영 방식 때문에 차이가 날 수 있습니다.";
            case PREFERRED_FEE_REFUND_POSSIBLE -> "우대수수료 환급이 반영되어 차이가 날 수 있습니다.";
            case SETTLEMENT_METHOD_REVIEW_REQUIRED -> "정산방식 차이 여부를 추가 확인해야 합니다.";
            case CONTEXT_OPTION_REVIEW_REQUIRED -> "업로드 파일 외 정책성 옵션 입력이 있으면 판정이 더 정확해집니다.";
        };
    }

    public Long getId() {
        return id;
    }

    public String getSourceType() {
        return sourceType;
    }

    public Long getSnapshotId() {
        return snapshotId;
    }

    public String getIssueType() {
        return issueType;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public String getProductOrderNo() {
        return productOrderNo;
    }

    public String getJoinKey() {
        return joinKey;
    }

    public String getMessage() {
        return message;
    }

    public boolean isResolved() {
        return resolved;
    }

    public String getSeverity() {
        return severity;
    }

    public String getJudgementStatus() {
        return judgementStatus;
    }

    public String getExplanationCode() {
        return explanationCode;
    }

    public boolean isNeedsUserInput() {
        return needsUserInput;
    }

    public String getPossibleReasonMessage() {
        return possibleReasonMessage;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}