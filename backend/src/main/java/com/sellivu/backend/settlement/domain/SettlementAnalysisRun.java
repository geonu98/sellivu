package com.sellivu.backend.settlement.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "settlement_analysis_run",
        indexes = {
                @Index(name = "idx_analysis_run_workspace_id", columnList = "workspace_id"),
                @Index(name = "idx_analysis_run_workspace_status", columnList = "workspace_id, status")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SettlementAnalysisRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "workspace_id", nullable = false)
    private Long workspaceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SettlementAnalysisRunStatus status;

    @Column(name = "daily_upload_id")
    private Long dailyUploadId;

    @Column(name = "order_upload_id")
    private Long orderUploadId;

    @Column(name = "fee_upload_id")
    private Long feeUploadId;

    @Column(name = "daily_row_count", nullable = false)
    private int dailyRowCount;

    @Column(name = "order_row_count", nullable = false)
    private int orderRowCount;

    @Column(name = "fee_row_count", nullable = false)
    private int feeRowCount;

    @Column(name = "snapshot_count", nullable = false)
    private int snapshotCount;

    @Column(name = "issue_count", nullable = false)
    private int issueCount;

    @Column(name = "error_message", columnDefinition = "text")
    private String errorMessage;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    private SettlementAnalysisRun(
            Long workspaceId,
            SettlementAnalysisRunStatus status,
            Long dailyUploadId,
            Long orderUploadId,
            Long feeUploadId,
            int dailyRowCount,
            int orderRowCount,
            int feeRowCount,
            int snapshotCount,
            int issueCount,
            String errorMessage,
            LocalDateTime startedAt,
            LocalDateTime finishedAt,
            LocalDateTime createdAt
    ) {
        this.workspaceId = workspaceId;
        this.status = status;
        this.dailyUploadId = dailyUploadId;
        this.orderUploadId = orderUploadId;
        this.feeUploadId = feeUploadId;
        this.dailyRowCount = dailyRowCount;
        this.orderRowCount = orderRowCount;
        this.feeRowCount = feeRowCount;
        this.snapshotCount = snapshotCount;
        this.issueCount = issueCount;
        this.errorMessage = errorMessage;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
        this.createdAt = createdAt;
    }

    public static SettlementAnalysisRun create(
            Long workspaceId,
            Long dailyUploadId,
            Long orderUploadId,
            Long feeUploadId
    ) {
        LocalDateTime now = LocalDateTime.now();
        return new SettlementAnalysisRun(
                workspaceId,
                SettlementAnalysisRunStatus.PENDING,
                dailyUploadId,
                orderUploadId,
                feeUploadId,
                0,
                0,
                0,
                0,
                0,
                null,
                now,
                null,
                now
        );
    }

    public void markRawLoading() {
        this.status = SettlementAnalysisRunStatus.RAW_LOADING;
    }

    public void markAnalyzing() {
        this.status = SettlementAnalysisRunStatus.ANALYZING;
    }

    public void markCompleted(int snapshotCount, int issueCount) {
        this.status = SettlementAnalysisRunStatus.COMPLETED;
        this.snapshotCount = snapshotCount;
        this.issueCount = issueCount;
        this.finishedAt = LocalDateTime.now();
    }

    public void markFailed(String errorMessage) {
        this.status = SettlementAnalysisRunStatus.FAILED;
        this.errorMessage = errorMessage;
        this.finishedAt = LocalDateTime.now();
    }

    public void updateRowCounts(int dailyRowCount, int orderRowCount, int feeRowCount) {
        this.dailyRowCount = dailyRowCount;
        this.orderRowCount = orderRowCount;
        this.feeRowCount = feeRowCount;
    }
}