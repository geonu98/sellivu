package com.sellivu.backend.settlement.repository;

import com.sellivu.backend.settlement.domain.SettlementFileType;
import com.sellivu.backend.settlement.domain.SettlementWorkspaceFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SettlementWorkspaceFileRepository extends JpaRepository<SettlementWorkspaceFile, Long> {

    List<SettlementWorkspaceFile> findAllByWorkspaceIdOrderByIdAsc(Long workspaceId);

    List<SettlementWorkspaceFile> findAllByWorkspaceIdAndActiveTrueOrderByIdAsc(Long workspaceId);

    Optional<SettlementWorkspaceFile> findByWorkspaceIdAndFileTypeAndActiveTrue(
            Long workspaceId,
            SettlementFileType fileType
    );

    Optional<SettlementWorkspaceFile> findByIdAndWorkspaceIdAndActiveTrue(
            Long id,
            Long workspaceId
    );

    Optional<SettlementWorkspaceFile> findByWorkspaceIdAndUploadId(
            Long workspaceId,
            Long uploadId
    );

    boolean existsByWorkspaceIdAndUploadId(Long workspaceId, Long uploadId);

    List<SettlementWorkspaceFile> findAllByWorkspaceIdAndFileTypeAndActiveTrueOrderByIdAsc(
            Long workspaceId,
            SettlementFileType fileType
    );
}