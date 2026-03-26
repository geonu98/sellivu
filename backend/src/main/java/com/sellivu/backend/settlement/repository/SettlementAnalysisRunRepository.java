package com.sellivu.backend.settlement.repository;

import com.sellivu.backend.settlement.domain.SettlementAnalysisRun;
import com.sellivu.backend.settlement.domain.SettlementAnalysisRunStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SettlementAnalysisRunRepository extends JpaRepository<SettlementAnalysisRun, Long> {

    List<SettlementAnalysisRun> findAllByWorkspaceIdOrderByIdDesc(Long workspaceId);

    Optional<SettlementAnalysisRun> findTopByWorkspaceIdOrderByIdDesc(Long workspaceId);

    List<SettlementAnalysisRun> findAllByWorkspaceIdAndStatusOrderByIdDesc(
            Long workspaceId,
            SettlementAnalysisRunStatus status
    );
}