package com.sellivu.backend.settlement.service;

import com.sellivu.backend.settlement.domain.SettlementUpload;
import com.sellivu.backend.settlement.domain.SettlementWorkspace;
import com.sellivu.backend.settlement.domain.SettlementWorkspaceFile;
import com.sellivu.backend.settlement.dto.SettlementUploadResponse;
import com.sellivu.backend.settlement.dto.WorkspaceFileResponse;
import com.sellivu.backend.settlement.dto.WorkspaceUploadResponse;
import com.sellivu.backend.settlement.repository.SettlementWorkspaceFileRepository;
import com.sellivu.backend.workspace.service.WorkspaceAnalysisSetLinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class SettlementWorkspaceUploadService {

    private final SettlementWorkspaceService settlementWorkspaceService;
    private final SettlementUploadService settlementUploadService;
    private final SettlementWorkspaceFileRepository workspaceFileRepository;
    private final WorkspaceAnalysisSetLinkService workspaceAnalysisSetLinkService;

    public WorkspaceUploadResponse uploadAndAttach(
            String workspaceKey,
            String workspaceToken,
            MultipartFile file
    ) {
        SettlementWorkspace workspace =
                settlementWorkspaceService.validateAccessibleWorkspace(workspaceKey, workspaceToken);

        SettlementUpload upload = settlementUploadService.getOrCreateUpload(file);

        Optional<SettlementWorkspaceFile> existingLinked =
                workspaceFileRepository.findByWorkspaceIdAndUploadId(workspace.getId(), upload.getId());

        if (existingLinked.isPresent()) {
            SettlementWorkspaceFile existing = existingLinked.get();

            if (!existing.isActive()) {
                Optional<SettlementWorkspaceFile> existingActive =
                        workspaceFileRepository.findByWorkspaceIdAndFileTypeAndActiveTrue(
                                workspace.getId(),
                                upload.getFileType()
                        );
                existingActive.ifPresent(SettlementWorkspaceFile::deactivate);

                existing.activate();
                workspaceAnalysisSetLinkService.refresh(workspace.getId());
            }

            return WorkspaceUploadResponse.of(
                    SettlementUploadResponse.from(upload, "기존 업로드 파일을 현재 워크스페이스에 다시 연결했습니다."),
                    WorkspaceFileResponse.of(existing, upload)
            );
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

        return WorkspaceUploadResponse.of(
                SettlementUploadResponse.from(upload, "정산 파일을 현재 워크스페이스에 연결했습니다."),
                WorkspaceFileResponse.of(saved, upload)
        );
    }
}