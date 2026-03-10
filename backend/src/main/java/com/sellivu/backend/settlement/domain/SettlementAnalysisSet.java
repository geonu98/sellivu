package com.sellivu.backend.settlement.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "settlement_analysis_set")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SettlementAnalysisSet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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
}