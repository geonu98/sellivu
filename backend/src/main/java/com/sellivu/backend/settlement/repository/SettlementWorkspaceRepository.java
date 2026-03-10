package com.sellivu.backend.settlement.repository;

import com.sellivu.backend.settlement.domain.SettlementWorkspace;
import com.sellivu.backend.settlement.domain.WorkspaceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SettlementWorkspaceRepository extends JpaRepository<SettlementWorkspace, Long> {

    Optional<SettlementWorkspace> findByWorkspaceKey(String workspaceKey);

    List<SettlementWorkspace> findAllByStatusAndExpiresAtBefore(
            WorkspaceStatus status,
            LocalDateTime expiresAt
    );
}