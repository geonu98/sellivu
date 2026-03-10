package com.sellivu.backend.settlement.controller;

import com.sellivu.backend.settlement.dto.*;
import com.sellivu.backend.settlement.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/settlement/workspaces")
@RequiredArgsConstructor
public class SettlementWorkspaceController {

    private static final String WORKSPACE_TOKEN_HEADER = "X-Workspace-Token";

    private final SettlementWorkspaceService settlementWorkspaceService;
    private final SettlementWorkspaceContextService settlementWorkspaceContextService;
    private final SettlementWorkspaceUploadService settlementWorkspaceUploadService;
    private final SettlementWorkspaceCapabilityService settlementWorkspaceCapabilityService;
    private final SettlementWorkspaceIssueService settlementWorkspaceIssueService;
    private final SettlementWorkspaceSaveService settlementWorkspaceSaveService;

    @PostMapping
    public WorkspaceCreateResponse createWorkspace() {
        return settlementWorkspaceService.createWorkspace(null);
    }

    @GetMapping("/{workspaceKey}")
    public WorkspaceResponse getWorkspace(
            @PathVariable String workspaceKey,
            @RequestHeader(WORKSPACE_TOKEN_HEADER) String workspaceToken
    ) {
        return settlementWorkspaceService.getWorkspace(workspaceKey, workspaceToken);
    }

    @GetMapping("/{workspaceKey}/context")
    public WorkspaceContextResponse getContext(
            @PathVariable String workspaceKey,
            @RequestHeader(WORKSPACE_TOKEN_HEADER) String workspaceToken
    ) {
        return settlementWorkspaceContextService.getContext(workspaceKey, workspaceToken);
    }

    @PutMapping("/{workspaceKey}/context")
    public WorkspaceContextResponse updateContext(
            @PathVariable String workspaceKey,
            @RequestHeader(WORKSPACE_TOKEN_HEADER) String workspaceToken,
            @RequestBody UpdateWorkspaceContextRequest request
    ) {
        return settlementWorkspaceContextService.updateContext(workspaceKey, workspaceToken, request);
    }

    @PostMapping(
            value = "/{workspaceKey}/uploads",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public WorkspaceUploadResponse upload(
            @PathVariable String workspaceKey,
            @RequestHeader(WORKSPACE_TOKEN_HEADER) String workspaceToken,
            @RequestPart("file") MultipartFile file
    ) {
        return settlementWorkspaceUploadService.uploadAndAttach(workspaceKey, workspaceToken, file);
    }

    @GetMapping("/{workspaceKey}/capability")
    public AnalysisCapabilityResponse getCapability(
            @PathVariable String workspaceKey,
            @RequestHeader(WORKSPACE_TOKEN_HEADER) String workspaceToken
    ) {
        return settlementWorkspaceCapabilityService.getCapability(workspaceKey, workspaceToken);
    }

    @GetMapping("/{workspaceKey}/issues")
    public List<SettlementAnalysisIssueResponse> getIssues(
            @PathVariable String workspaceKey,
            @RequestHeader(WORKSPACE_TOKEN_HEADER) String workspaceToken
    ) {
        return settlementWorkspaceIssueService.getIssues(workspaceKey, workspaceToken);
    }

    @PostMapping("/{workspaceKey}/save")
    public WorkspaceSaveResponse save(
            @PathVariable String workspaceKey,
            @RequestHeader(WORKSPACE_TOKEN_HEADER) String workspaceToken,
            @RequestBody WorkspaceSaveRequest request
    ) {
        return settlementWorkspaceSaveService.save(workspaceKey, workspaceToken, null, request);
    }
}