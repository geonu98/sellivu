package com.sellivu.backend.settlement.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "settlement_issue",
        indexes = {
                @Index(name = "idx_issue_snapshot_id", columnList = "snapshotId"),
                @Index(name = "idx_issue_type", columnList = "issueType"),
                @Index(name = "idx_issue_resolved", columnList = "resolved")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SettlementIssue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long snapshotId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private SettlementIssueType issueType;

    @Column(length = 100)
    private String orderNo;

    @Column(length = 100)
    private String productOrderNo;

    @Column(length = 100)
    private String joinKey;

    @Column(length = 1000)
    private String message;

    @Column(nullable = false)
    private boolean resolved;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 20)
    private IssueSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(name = "judgement_status", nullable = false, length = 30)
    private IssueJudgementStatus judgementStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "explanation_code", length = 50)
    private IssueExplanationCode explanationCode;

    @Column(name = "needs_user_input", nullable = false)
    private boolean needsUserInput;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Builder
    private SettlementIssue(
            Long snapshotId,
            SettlementIssueType issueType,
            String orderNo,
            String productOrderNo,
            String joinKey,
            String message,
            boolean resolved,
            IssueSeverity severity,
            IssueJudgementStatus judgementStatus,
            IssueExplanationCode explanationCode,
            boolean needsUserInput,
            LocalDateTime createdAt
    ) {
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

    public static SettlementIssue create(
            Long snapshotId,
            SettlementIssueType issueType,
            String orderNo,
            String productOrderNo,
            String joinKey,
            String message
    ) {
        return createDetailed(
                snapshotId,
                issueType,
                orderNo,
                productOrderNo,
                joinKey,
                message,
                IssueSeverity.ERROR,
                IssueJudgementStatus.CONFIRMED,
                null,
                false
        );
    }

    public static SettlementIssue createDetailed(
            Long snapshotId,
            SettlementIssueType issueType,
            String orderNo,
            String productOrderNo,
            String joinKey,
            String message,
            IssueSeverity severity,
            IssueJudgementStatus judgementStatus,
            IssueExplanationCode explanationCode,
            boolean needsUserInput
    ) {
        return SettlementIssue.builder()
                .snapshotId(snapshotId)
                .issueType(issueType)
                .orderNo(orderNo)
                .productOrderNo(productOrderNo)
                .joinKey(joinKey)
                .message(message)
                .resolved(false)
                .severity(severity)
                .judgementStatus(judgementStatus)
                .explanationCode(explanationCode)
                .needsUserInput(needsUserInput)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public void resolve() {
        this.resolved = true;
    }
}