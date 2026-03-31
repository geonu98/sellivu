package com.sellivu.backend.workspace.service;

import com.sellivu.backend.global.error.ApiException;
import com.sellivu.backend.global.error.ErrorCode;
import com.sellivu.backend.settlement.domain.SettlementWorkspace;
import com.sellivu.backend.settlement.domain.SettlementWorkspaceFile;
import com.sellivu.backend.settlement.dto.SettlementAnalysisSetResponse;
import com.sellivu.backend.settlement.repository.SettlementWorkspaceFileRepository;
import com.sellivu.backend.settlement.repository.SettlementWorkspaceRepository;
import com.sellivu.backend.settlement.service.SettlementAnalysisSetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class WorkspaceAnalysisSetLinkService {

    private final SettlementWorkspaceRepository workspaceRepository;
    private final SettlementWorkspaceFileRepository workspaceFileRepository;
    private final SettlementAnalysisSetService settlementAnalysisSetService;

    public Long refresh(Long workspaceId) {
        SettlementWorkspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ApiException(ErrorCode.WORKSPACE_NOT_FOUND));

        List<SettlementWorkspaceFile> activeFiles = workspaceFileRepository
                .findAllByWorkspaceIdAndActiveTrueOrderByIdAsc(workspace.getId()).stream()
                .sorted(Comparator.comparing(SettlementWorkspaceFile::getCreatedAt))
                .toList();

        if (activeFiles.isEmpty()) {
            workspace.clearSavedAnalysisSetId();
            return null;
        }

        SettlementAnalysisSetResponse createdSet =
                settlementAnalysisSetService.createSet(buildSetName(workspace));

        Long analysisSetId = extractAnalysisSetId(createdSet);

        for (SettlementWorkspaceFile workspaceFile : activeFiles) {
            settlementAnalysisSetService.addUploadToSet(analysisSetId, workspaceFile.getUploadId());
        }

        workspace.updateSavedAnalysisSetId(analysisSetId);

        return analysisSetId;
    }

    private String buildSetName(SettlementWorkspace workspace) {
        return "workspace-" + workspace.getWorkspaceKey();
    }

    private Long extractAnalysisSetId(SettlementAnalysisSetResponse response) {
        Long analysisSetId = response.getId();

        if (analysisSetId == null) {
            throw new ApiException(
                    ErrorCode.INTERNAL_SERVER_ERROR,
                    "SettlementAnalysisSetResponse id가 비어 있습니다."
            );
        }

        return analysisSetId;
    }
}