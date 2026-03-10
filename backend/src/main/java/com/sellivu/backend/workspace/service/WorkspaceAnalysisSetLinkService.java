package com.sellivu.backend.workspace.service;

import com.sellivu.backend.global.error.ApiException;
import com.sellivu.backend.global.error.ErrorCode;
import com.sellivu.backend.settlement.domain.SettlementWorkspace;
import com.sellivu.backend.settlement.domain.SettlementWorkspaceFile;
import com.sellivu.backend.settlement.dto.SettlementAnalysisSetResponse;
import com.sellivu.backend.settlement.repository.SettlementWorkspaceFileRepository;
import com.sellivu.backend.settlement.repository.SettlementWorkspaceRepository;
import com.sellivu.backend.settlement.service.SettlementAnalysisRebuildService;
import com.sellivu.backend.settlement.service.SettlementAnalysisSetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class WorkspaceAnalysisSetLinkService {

    private final SettlementWorkspaceRepository workspaceRepository;
    private final SettlementWorkspaceFileRepository workspaceFileRepository;
    private final SettlementAnalysisSetService settlementAnalysisSetService;
    private final SettlementAnalysisRebuildService settlementAnalysisRebuildService;

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

        settlementAnalysisRebuildService.rebuildSet(analysisSetId);
        workspace.updateSavedAnalysisSetId(analysisSetId);

        return analysisSetId;
    }

    private String buildSetName(SettlementWorkspace workspace) {
        return "workspace-" + workspace.getWorkspaceKey();
    }

    private Long extractAnalysisSetId(SettlementAnalysisSetResponse response) {
        try {
            Method getter = response.getClass().getMethod("getId");
            Object value = getter.invoke(response);
            return (Long) value;
        } catch (Exception ignored) {
        }

        try {
            Method accessor = response.getClass().getMethod("id");
            Object value = accessor.invoke(response);
            return (Long) value;
        } catch (Exception ignored) {
        }

        throw new ApiException(
                ErrorCode.INTERNAL_SERVER_ERROR,
                "SettlementAnalysisSetResponse에서 id를 읽을 수 없습니다."
        );
    }
}