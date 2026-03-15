package com.sellivu.backend.settlement.service;

import com.sellivu.backend.global.error.ApiException;
import com.sellivu.backend.global.error.ErrorCode;
import com.sellivu.backend.settlement.domain.SettlementAnalysisContext;
import com.sellivu.backend.settlement.domain.SettlementAnalysisSet;
import com.sellivu.backend.settlement.domain.SettlementAnalysisSetItem;
import com.sellivu.backend.settlement.domain.SettlementWorkspace;
import com.sellivu.backend.settlement.domain.SettlementWorkspaceContext;
import com.sellivu.backend.settlement.domain.SettlementWorkspaceFile;
import com.sellivu.backend.settlement.dto.WorkspaceSaveRequest;
import com.sellivu.backend.settlement.dto.WorkspaceSaveResponse;
import com.sellivu.backend.settlement.repository.SettlementAnalysisContextRepository;
import com.sellivu.backend.settlement.repository.SettlementAnalysisSetItemRepository;
import com.sellivu.backend.settlement.repository.SettlementAnalysisSetRepository;
import com.sellivu.backend.settlement.repository.SettlementWorkspaceContextRepository;
import com.sellivu.backend.settlement.repository.SettlementWorkspaceFileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SettlementWorkspaceSaveService {

    private final SettlementWorkspaceService settlementWorkspaceService;
    private final SettlementWorkspaceFileRepository workspaceFileRepository;
    private final SettlementWorkspaceContextRepository workspaceContextRepository;
    private final SettlementAnalysisSetRepository analysisSetRepository;
    private final SettlementAnalysisSetItemRepository analysisSetItemRepository;
    private final SettlementAnalysisContextRepository analysisContextRepository;
    private final SettlementAnalysisIssueService settlementAnalysisIssueService;

    public WorkspaceSaveResponse save(
            String workspaceKey,
            String workspaceToken,
            Long userId,
            WorkspaceSaveRequest request
    ) {
        SettlementWorkspace workspace =
                settlementWorkspaceService.validateAccessibleWorkspace(workspaceKey, workspaceToken);

        Long resolvedUserId = resolveUserId(userId);

        if (resolvedUserId == null) {
            throw new ApiException(ErrorCode.WORKSPACE_SAVE_REQUIRES_LOGIN);
        }

        if (!workspace.isActive()) {
            throw new ApiException(ErrorCode.WORKSPACE_NOT_ACTIVE);
        }

        if (workspace.getUserId() == null) {
            workspace.assignUser(resolvedUserId);
        }

        List<SettlementWorkspaceFile> files =
                workspaceFileRepository.findAllByWorkspaceIdAndActiveTrueOrderByIdAsc(workspace.getId());

        if (files.isEmpty()) {
            throw new ApiException(ErrorCode.WORKSPACE_NO_FILES);
        }

        String finalName = (request == null || request.name() == null || request.name().isBlank())
                ? "기본 분석 세트"
                : request.name().trim();

        SettlementAnalysisSet savedSet = SettlementAnalysisSet.create(finalName);
        savedSet.assignUser(resolvedUserId);
        savedSet = analysisSetRepository.save(savedSet);

        for (SettlementWorkspaceFile file : files) {
            analysisSetItemRepository.save(
                    SettlementAnalysisSetItem.create(
                            savedSet.getId(),
                            file.getUploadId(),
                            file.getFileType()
                    )
            );
        }

        SettlementWorkspaceContext workspaceContext = workspaceContextRepository.findByWorkspaceId(workspace.getId())
                .orElseThrow(() -> new ApiException(ErrorCode.WORKSPACE_CONTEXT_NOT_FOUND));

        SettlementAnalysisContext analysisContext =
                SettlementAnalysisContext.createDefault(savedSet.getId());

        analysisContext.update(
                workspaceContext.getStoreCouponUsage(),
                workspaceContext.getNaverCouponUsage(),
                workspaceContext.getPointBenefitUsage(),
                workspaceContext.getSafeReturnCareUsage(),
                workspaceContext.getBizWalletOffsetUsage(),
                workspaceContext.getFastSettlementUsage(),
                workspaceContext.getClaimIncluded()
        );

        analysisContextRepository.save(analysisContext);

        settlementAnalysisIssueService.rebuildIssues(savedSet);

        workspace.markSaved(savedSet.getId());

        return WorkspaceSaveResponse.from(savedSet);
    }

    private Long resolveUserId(Long userId) {
        if (userId != null) {
            return userId;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            return null;
        }

        Object principal = authentication.getPrincipal();

        try {
            Method getter = principal.getClass().getMethod("getUserId");
            Object value = getter.invoke(principal);
            if (value instanceof Long longValue) {
                return longValue;
            }
            if (value instanceof Number number) {
                return number.longValue();
            }
        } catch (Exception ignored) {
        }

        try {
            Method getter = principal.getClass().getMethod("getId");
            Object value = getter.invoke(principal);
            if (value instanceof Long longValue) {
                return longValue;
            }
            if (value instanceof Number number) {
                return number.longValue();
            }
        } catch (Exception ignored) {
        }

        return null;
    }
}