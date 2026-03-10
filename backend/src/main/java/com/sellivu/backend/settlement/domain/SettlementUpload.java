package com.sellivu.backend.settlement.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "settlement_upload",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_settlement_upload_file_hash",
                        columnNames = "file_hash"
                )
        }
)
public class SettlementUpload {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "original_file_name", nullable = false, length = 255)
    private String originalFileName;

    @Column(name = "stored_file_name", length = 255)
    private String storedFileName;

    @Column(name = "file_hash", nullable = false, length = 64)
    private String fileHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "file_type", length = 50)
    private SettlementFileType fileType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private SettlementUploadStatus status;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

    @Column(name = "parsed_at")
    private LocalDateTime parsedAt;

    @Column(name = "failed_at")
    private LocalDateTime failedAt;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Column(name = "analysis_job_id")
    private Long analysisJobId;

    protected SettlementUpload() {
    }

    public SettlementUpload(
            String originalFileName,
            String storedFileName,
            String fileHash,
            SettlementFileType fileType,
            SettlementUploadStatus status
    ) {
        this.originalFileName = originalFileName;
        this.storedFileName = storedFileName;
        this.fileHash = fileHash;
        this.fileType = fileType;
        this.status = status;
        this.uploadedAt = LocalDateTime.now();
    }

    public static SettlementUpload uploaded(
            String originalFileName,
            String storedFileName,
            String fileHash,
            SettlementFileType fileType
    ) {
        return new SettlementUpload(
                originalFileName,
                storedFileName,
                fileHash,
                fileType,
                SettlementUploadStatus.UPLOADED
        );
    }

    public void markParsing() {
        this.status = SettlementUploadStatus.PARSING;
    }

    public void markParsed() {
        this.status = SettlementUploadStatus.PARSED;
        this.parsedAt = LocalDateTime.now();
    }

    public void markDuplicate() {
        this.status = SettlementUploadStatus.DUPLICATE;
    }

    public void markFailed(String errorMessage) {
        this.status = SettlementUploadStatus.FAILED;
        this.failedAt = LocalDateTime.now();
        this.errorMessage = errorMessage;
    }

    public void assignAnalysisJob(Long analysisJobId) {
        this.analysisJobId = analysisJobId;
    }

    public Long getId() {
        return id;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public String getStoredFileName() {
        return storedFileName;
    }

    public String getFileHash() {
        return fileHash;
    }

    public SettlementFileType getFileType() {
        return fileType;
    }

    public SettlementUploadStatus getStatus() {
        return status;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public LocalDateTime getParsedAt() {
        return parsedAt;
    }

    public LocalDateTime getFailedAt() {
        return failedAt;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Long getAnalysisJobId() {
        return analysisJobId;
    }
}