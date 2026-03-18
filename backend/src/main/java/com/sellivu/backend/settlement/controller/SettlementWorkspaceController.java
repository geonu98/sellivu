package com.sellivu.backend.settlement.controller;

import com.sellivu.backend.settlement.dto.*;
import com.sellivu.backend.settlement.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.Authentication;

import java.lang.reflect.Method;
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
    public WorkspaceCreateResponse createWorkspace(Authentication authentication) {
        System.out.println("createWorkspace authentication = " + authentication);
        System.out.println("createWorkspace principal = " +
                (authentication != null ? authentication.getPrincipal() : null));

        Long userId = resolveUserId(authentication);
        System.out.println("createWorkspace resolvedUserId = " + userId);

        return settlementWorkspaceService.createWorkspace(userId);
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
    }@PostMapping("/{workspaceKey}/save")
    public WorkspaceSaveResponse save(
            @PathVariable String workspaceKey,
            @RequestHeader(WORKSPACE_TOKEN_HEADER) String workspaceToken,
            @RequestBody WorkspaceSaveRequest request,
            Authentication authentication
    ) {
        System.out.println("save authentication = " + authentication);
        System.out.println("save principal = " +
                (authentication != null ? authentication.getPrincipal() : null));

        Long userId = resolveUserId(authentication);
        System.out.println("save resolvedUserId = " + userId);

        return settlementWorkspaceSaveService.save(workspaceKey, workspaceToken, userId, request);
    }

    @DeleteMapping("/{workspaceKey}/files/{workspaceFileId}")
    public ResponseEntity<Void> removeWorkspaceFile(
            @PathVariable String workspaceKey,
            @PathVariable Long workspaceFileId,
            @RequestHeader("X-Workspace-Token") String workspaceToken
    ) {
        settlementWorkspaceService.removeWorkspaceFile(workspaceKey, workspaceToken, workspaceFileId);
        return ResponseEntity.noContent().build();
    }

    private Long resolveUserId(Authentication authentication) {
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