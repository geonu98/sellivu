package com.sellivu.backend.settlement.service;

import com.sellivu.backend.global.error.ApiException;
import com.sellivu.backend.global.error.ErrorCode;
import com.sellivu.backend.settlement.domain.SettlementUpload;
import com.sellivu.backend.settlement.domain.SettlementWorkspace;
import com.sellivu.backend.settlement.domain.SettlementWorkspaceFile;
import com.sellivu.backend.settlement.dto.SettlementUploadResponse;
import com.sellivu.backend.settlement.dto.WorkspaceFileResponse;
import com.sellivu.backend.settlement.dto.WorkspaceUploadResponse;
import com.sellivu.backend.settlement.repository.SettlementUploadRepository;
import com.sellivu.backend.settlement.repository.SettlementWorkspaceFileRepository;
import com.sellivu.backend.workspace.service.WorkspaceAnalysisSetLinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class SettlementWorkspaceUploadService {

    private final SettlementWorkspaceService settlementWorkspaceService;
    private final SettlementUploadService settlementUploadService;
    private final SettlementWorkspaceFileRepository workspaceFileRepository;
    private final SettlementUploadRepository settlementUploadRepository;
    private final WorkspaceAnalysisSetLinkService workspaceAnalysisSetLinkService;

    public WorkspaceUploadResponse uploadAndAttach(
            String workspaceKey,
            String workspaceToken,
            MultipartFile file
    ) {
        SettlementWorkspace workspace =
                settlementWorkspaceService.validateAccessibleWorkspace(workspaceKey, workspaceToken);

        SettlementUploadResponse uploadResponse = settlementUploadService.upload(file);
        Long uploadId = extractUploadId(uploadResponse);

        SettlementUpload upload = settlementUploadRepository.findById(uploadId)
                .orElseThrow(() -> new ApiException(
                        ErrorCode.INVALID_INPUT_VALUE,
                        "업로드 파일을 찾을 수 없습니다. uploadId=" + uploadId
                ));

        if (workspaceFileRepository.existsByWorkspaceIdAndUploadId(workspace.getId(), uploadId)) {
            SettlementWorkspaceFile existing = workspaceFileRepository
                    .findAllByWorkspaceIdAndActiveTrueOrderByIdAsc(workspace.getId()).stream()
                    .filter(it -> it.getUploadId().equals(uploadId))
                    .findFirst()
                    .orElseThrow(() -> new ApiException(
                            ErrorCode.INVALID_INPUT_VALUE,
                            "이미 workspace에 연결된 업로드입니다. uploadId=" + uploadId
                    ));

            return WorkspaceUploadResponse.of(uploadResponse, WorkspaceFileResponse.of(existing, upload));
        }

        Optional<SettlementWorkspaceFile> existingActive =
                workspaceFileRepository.findByWorkspaceIdAndFileTypeAndActiveTrue(
                        workspace.getId(),
                        upload.getFileType()
                );

        existingActive.ifPresent(SettlementWorkspaceFile::deactivate);

        SettlementWorkspaceFile saved = workspaceFileRepository.save(
                SettlementWorkspaceFile.create(
                        workspace.getId(),
                        upload.getId(),
                        upload.getFileType()
                )
        );

        workspaceAnalysisSetLinkService.refresh(workspace.getId());

        return WorkspaceUploadResponse.of(uploadResponse, WorkspaceFileResponse.of(saved, upload));
    }

    private Long extractUploadId(Object uploadResponse) {
        try {
            Method getter = uploadResponse.getClass().getMethod("getUploadId");
            Object value = getter.invoke(uploadResponse);
            return (Long) value;
        } catch (Exception ignored) {
        }

        try {
            Method accessor = uploadResponse.getClass().getMethod("uploadId");
            Object value = accessor.invoke(uploadResponse);
            return (Long) value;
        } catch (Exception ignored) {
        }

        throw new ApiException(
                ErrorCode.INTERNAL_SERVER_ERROR,
                "SettlementUploadResponse에서 uploadId를 읽을 수 없습니다."
        );
    }
}