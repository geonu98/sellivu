package com.sellivu.backend.settlement.repository;

import com.sellivu.backend.settlement.domain.SettlementAnalysisContext;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SettlementAnalysisContextRepository extends JpaRepository<SettlementAnalysisContext, Long> {
    Optional<SettlementAnalysisContext> findByAnalysisSetId(Long analysisSetId);
    boolean existsByAnalysisSetId(Long analysisSetId);
}