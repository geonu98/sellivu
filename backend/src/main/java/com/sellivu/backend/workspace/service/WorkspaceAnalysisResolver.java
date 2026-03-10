package com.sellivu.backend.workspace.service;

import com.sellivu.backend.global.error.ApiException;
import com.sellivu.backend.global.error.ErrorCode;
import com.sellivu.backend.settlement.domain.SettlementWorkspace;
import com.sellivu.backend.settlement.service.SettlementWorkspaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkspaceAnalysisResolver {

    private final SettlementWorkspaceService settlementWorkspaceService;

    public Long resolveAnalysisSetId(String workspaceKey, String workspaceToken) {
        SettlementWorkspace workspace =
                settlementWorkspaceService.validateAccessibleWorkspace(workspaceKey, workspaceToken);

        if (workspace.getSavedAnalysisSetId() == null) {
            throw new ApiException(ErrorCode.WORKSPACE_ANALYSIS_SET_NOT_READY);
        }

        return workspace.getSavedAnalysisSetId();
    }
}