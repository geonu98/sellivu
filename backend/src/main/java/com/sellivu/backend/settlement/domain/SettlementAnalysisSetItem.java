package com.sellivu.backend.settlement.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "settlement_analysis_set_item",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_analysis_set_item_set_upload",
                        columnNames = {"analysis_set_id", "upload_id"}
                )
        },
        indexes = {
                @Index(name = "idx_analysis_set_item_set_id", columnList = "analysis_set_id"),
                @Index(name = "idx_analysis_set_item_upload_id", columnList = "upload_id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SettlementAnalysisSetItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "analysis_set_id", nullable = false)
    private Long analysisSetId;

    @Column(name = "upload_id", nullable = false)
    private Long uploadId;

    @Enumerated(EnumType.STRING)
    @Column(name = "file_type", nullable = false, length = 50)
    private SettlementFileType fileType;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected SettlementAnalysisSetItem(
            Long analysisSetId,
            Long uploadId,
            SettlementFileType fileType,
            LocalDateTime createdAt
    ) {
        this.analysisSetId = analysisSetId;
        this.uploadId = uploadId;
        this.fileType = fileType;
        this.createdAt = createdAt;
    }

    public static SettlementAnalysisSetItem create(
            Long analysisSetId,
            Long uploadId,
            SettlementFileType fileType
    ) {
        return new SettlementAnalysisSetItem(
                analysisSetId,
                uploadId,
                fileType,
                LocalDateTime.now()
        );
    }
}