package com.sellivu.backend.settlement.repository;

import com.sellivu.backend.settlement.domain.SettlementAnalysisSet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SettlementAnalysisSetRepository extends JpaRepository<SettlementAnalysisSet, Long> {

    List<SettlementAnalysisSet> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<SettlementAnalysisSet> findByIdAndUserId(Long id, Long userId);
}