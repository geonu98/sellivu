package com.sellivu.backend.settlement.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "settlement_analysis_set",
        indexes = {
                @Index(name = "idx_analysis_set_user_id_created_at", columnList = "user_id, created_at")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SettlementAnalysisSet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "name", length = 200)
    private String name;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected SettlementAnalysisSet(String name, LocalDateTime createdAt) {
        this.name = name;
        this.createdAt = createdAt;
    }

    public static SettlementAnalysisSet create(String name) {
        return new SettlementAnalysisSet(name, LocalDateTime.now());
    }

    public void assignUser(Long userId) {
        this.userId = userId;
    }
}