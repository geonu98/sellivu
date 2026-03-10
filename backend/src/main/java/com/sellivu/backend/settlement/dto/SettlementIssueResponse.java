package com.sellivu.backend.settlement.dto;

import com.sellivu.backend.settlement.domain.SettlementIssue;

import java.time.LocalDateTime;

public class SettlementIssueResponse {

    private final Long id;
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
    private final LocalDateTime createdAt;

    public SettlementIssueResponse(
            Long id,
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
            LocalDateTime createdAt
    ) {
        this.id = id;
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
        this.createdAt = createdAt;
    }

    public static SettlementIssueResponse from(SettlementIssue issue) {
        return new SettlementIssueResponse(
                issue.getId(),
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
                issue.getCreatedAt()
        );
    }

    public Long getId() {
        return id;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}