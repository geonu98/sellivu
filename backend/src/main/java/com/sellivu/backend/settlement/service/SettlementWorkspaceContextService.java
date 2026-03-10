package com.sellivu.backend.settlement.service;

import com.sellivu.backend.global.error.ApiException;
import com.sellivu.backend.global.error.ErrorCode;
import com.sellivu.backend.settlement.domain.SettlementWorkspace;
import com.sellivu.backend.settlement.domain.SettlementWorkspaceContext;
import com.sellivu.backend.settlement.dto.UpdateWorkspaceContextRequest;
import com.sellivu.backend.settlement.dto.WorkspaceContextResponse;
import com.sellivu.backend.settlement.repository.SettlementWorkspaceContextRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SettlementWorkspaceContextService {

    private final SettlementWorkspaceService settlementWorkspaceService;
    private final SettlementWorkspaceContextRepository workspaceContextRepository;

    @Transactional(readOnly = true)
    public WorkspaceContextResponse getContext(String workspaceKey, String workspaceToken) {
        SettlementWorkspace workspace =
                settlementWorkspaceService.validateAccessibleWorkspace(workspaceKey, workspaceToken);

        SettlementWorkspaceContext context = workspaceContextRepository.findByWorkspaceId(workspace.getId())
                .orElseThrow(() -> new ApiException(ErrorCode.WORKSPACE_CONTEXT_NOT_FOUND));

        return WorkspaceContextResponse.from(context);
    }

    public WorkspaceContextResponse updateContext(
            String workspaceKey,
            String workspaceToken,
            UpdateWorkspaceContextRequest request
    ) {
        SettlementWorkspace workspace =
                settlementWorkspaceService.validateAccessibleWorkspace(workspaceKey, workspaceToken);

        SettlementWorkspaceContext context = workspaceContextRepository.findByWorkspaceId(workspace.getId())
                .orElseThrow(() -> new ApiException(ErrorCode.WORKSPACE_CONTEXT_NOT_FOUND));

        context.update(
                request.storeCouponUsage(),
                request.naverCouponUsage(),
                request.pointBenefitUsage(),
                request.safeReturnCareUsage(),
                request.bizWalletOffsetUsage(),
                request.fastSettlementUsage(),
                request.claimIncluded()
        );

        return WorkspaceContextResponse.from(context);
    }
}