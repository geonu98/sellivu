package com.sellivu.backend.settlement.repository;

import com.sellivu.backend.settlement.domain.SettlementWorkspaceContext;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SettlementWorkspaceContextRepository extends JpaRepository<SettlementWorkspaceContext, Long> {

    Optional<SettlementWorkspaceContext> findByWorkspaceId(Long workspaceId);

    boolean existsByWorkspaceId(Long workspaceId);
}