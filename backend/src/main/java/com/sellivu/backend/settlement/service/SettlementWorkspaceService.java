package com.sellivu.backend.settlement.service;

import com.sellivu.backend.global.error.ApiException;
import com.sellivu.backend.global.error.ErrorCode;
import com.sellivu.backend.settlement.domain.SettlementUpload;
import com.sellivu.backend.settlement.domain.SettlementWorkspace;
import com.sellivu.backend.settlement.domain.SettlementWorkspaceContext;
import com.sellivu.backend.settlement.domain.SettlementWorkspaceFile;
import com.sellivu.backend.settlement.domain.WorkspaceStatus;
import com.sellivu.backend.settlement.dto.AnalysisCapabilityResponse;
import com.sellivu.backend.settlement.dto.WorkspaceContextResponse;
import com.sellivu.backend.settlement.dto.WorkspaceCreateResponse;
import com.sellivu.backend.settlement.dto.WorkspaceFileResponse;
import com.sellivu.backend.settlement.dto.WorkspaceResponse;
import com.sellivu.backend.settlement.repository.SettlementUploadRepository;
import com.sellivu.backend.settlement.repository.SettlementWorkspaceContextRepository;
import com.sellivu.backend.settlement.repository.SettlementWorkspaceFileRepository;
import com.sellivu.backend.settlement.repository.SettlementWorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SettlementWorkspaceService {

    private final SettlementWorkspaceRepository workspaceRepository;
    private final SettlementWorkspaceContextRepository workspaceContextRepository;
    private final SettlementWorkspaceFileRepository workspaceFileRepository;
    private final SettlementUploadRepository settlementUploadRepository;
    private final SettlementWorkspaceCapabilityService settlementWorkspaceCapabilityService;
    private final PasswordEncoder passwordEncoder;

    public WorkspaceCreateResponse createWorkspace(Long userId) {
        String rawToken = generateWorkspaceToken();
        String encodedToken = passwordEncoder.encode(rawToken);

        LocalDateTime expiresAt = userId == null
                ? LocalDateTime.now().plusDays(3)
                : LocalDateTime.now().plusDays(30);

        SettlementWorkspace workspace = (userId == null)
                ? SettlementWorkspace.createGuest(encodedToken, expiresAt)
                : SettlementWorkspace.createUser(userId, encodedToken, expiresAt);

        SettlementWorkspace savedWorkspace = workspaceRepository.save(workspace);

        workspaceContextRepository.save(
                SettlementWorkspaceContext.createDefault(savedWorkspace.getId())
        );

        return WorkspaceCreateResponse.of(savedWorkspace, rawToken);
    }

    @Transactional(readOnly = true)
    public WorkspaceResponse getWorkspace(String workspaceKey, String workspaceToken) {
        SettlementWorkspace workspace = validateAccessibleWorkspace(workspaceKey, workspaceToken);

        List<SettlementWorkspaceFile> workspaceFiles =
                workspaceFileRepository.findAllByWorkspaceIdAndActiveTrueOrderByIdAsc(workspace.getId());

        List<WorkspaceFileResponse> fileResponses = workspaceFiles.stream()
                .map(this::toWorkspaceFileResponse)
                .toList();

        SettlementWorkspaceContext context = workspaceContextRepository.findByWorkspaceId(workspace.getId())
                .orElseThrow(() -> new ApiException(ErrorCode.WORKSPACE_CONTEXT_NOT_FOUND));

        WorkspaceContextResponse contextResponse = WorkspaceContextResponse.from(context);

        AnalysisCapabilityResponse capabilityResponse =
                settlementWorkspaceCapabilityService.getCapability(workspaceKey, workspaceToken);

        return new WorkspaceResponse(
                workspace.getWorkspaceKey(),
                workspace.getOwnerType(),
                workspace.getStatus(),
                workspace.getSavedAnalysisSetId(),
                workspace.getExpiresAt(),
                fileResponses,
                contextResponse,
                capabilityResponse
        );
    }

    public SettlementWorkspace validateAccessibleWorkspace(String workspaceKey, String workspaceToken) {
        SettlementWorkspace workspace = workspaceRepository.findByWorkspaceKey(workspaceKey)
                .orElseThrow(() -> new ApiException(
                        ErrorCode.WORKSPACE_NOT_FOUND,
                        "워크스페이스를 찾을 수 없습니다. workspaceKey=" + workspaceKey
                ));

        if (workspace.isExpired()) {
            workspace.markExpired();
            throw new ApiException(
                    ErrorCode.WORKSPACE_EXPIRED,
                    "만료된 워크스페이스입니다. workspaceKey=" + workspaceKey
            );
        }

        if (!passwordEncoder.matches(workspaceToken, workspace.getAccessTokenHash())) {
            throw new ApiException(ErrorCode.WORKSPACE_ACCESS_DENIED);
        }

        if (workspace.getStatus() == WorkspaceStatus.EXPIRED || workspace.getStatus() == WorkspaceStatus.DELETED) {
            throw new ApiException(ErrorCode.WORKSPACE_NOT_ACTIVE);
        }

        workspace.touch();
        return workspace;
    }

    private WorkspaceFileResponse toWorkspaceFileResponse(SettlementWorkspaceFile workspaceFile) {
        SettlementUpload upload = settlementUploadRepository.findById(workspaceFile.getUploadId())
                .orElseThrow(() -> new ApiException(
                        ErrorCode.INVALID_INPUT_VALUE,
                        "업로드 파일을 찾을 수 없습니다. uploadId=" + workspaceFile.getUploadId()
                ));

        return WorkspaceFileResponse.of(workspaceFile, upload);
    }

    private String generateWorkspaceToken() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return "ws_" + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }


    @Transactional
    public void removeWorkspaceFile(String workspaceKey, String workspaceToken, Long workspaceFileId) {
        SettlementWorkspace workspace = validateAccessibleWorkspace(workspaceKey, workspaceToken);

        if (workspace.getStatus() != WorkspaceStatus.ACTIVE) {
            throw new ApiException(ErrorCode.WORKSPACE_NOT_ACTIVE, "활성 상태의 워크스페이스만 수정할 수 있습니다.");
        }

        SettlementWorkspaceFile workspaceFile = workspaceFileRepository
                .findByIdAndWorkspaceIdAndActiveTrue(workspaceFileId, workspace.getId())
                .orElseThrow(() -> new ApiException(
                        ErrorCode.WORKSPACE_FILE_NOT_FOUND,
                        "현재 워크스페이스에서 활성 상태인 파일을 찾을 수 없습니다."
                ));

        workspaceFile.deactivate();
    }
}