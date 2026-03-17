package com.sellivu.backend.settlement.service;

import com.sellivu.backend.global.error.ApiException;
import com.sellivu.backend.global.error.ErrorCode;
import com.sellivu.backend.settlement.domain.SettlementAnalysisContext;
import com.sellivu.backend.settlement.domain.SettlementAnalysisSet;
import com.sellivu.backend.settlement.domain.SettlementAnalysisSetItem;
import com.sellivu.backend.settlement.domain.SettlementUpload;
import com.sellivu.backend.settlement.dto.SettlementAnalysisSetItemResponse;
import com.sellivu.backend.settlement.dto.SettlementAnalysisSetResponse;
import com.sellivu.backend.settlement.dto.WorkspaceContextResponse;
import com.sellivu.backend.settlement.exception.AnalysisSetNotFoundException;
import com.sellivu.backend.settlement.exception.AnalysisSetUploadAlreadyExistsException;
import com.sellivu.backend.settlement.exception.SettlementUploadNotFoundException;
import com.sellivu.backend.settlement.repository.SettlementAnalysisContextRepository;
import com.sellivu.backend.settlement.repository.SettlementAnalysisSetItemRepository;
import com.sellivu.backend.settlement.repository.SettlementAnalysisSetRepository;
import com.sellivu.backend.settlement.repository.SettlementUploadRepository;
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
public class SettlementAnalysisSetService {

    private final SettlementAnalysisSetRepository analysisSetRepository;
    private final SettlementAnalysisSetItemRepository analysisSetItemRepository;
    private final SettlementUploadRepository settlementUploadRepository;
    private final SettlementAnalysisContextRepository settlementAnalysisContextRepository;

    public SettlementAnalysisSetResponse createSet(String name) {
        String finalName = (name == null || name.isBlank()) ? "기본 분석 세트" : name.trim();

        SettlementAnalysisSet saved = analysisSetRepository.save(SettlementAnalysisSet.create(finalName));

        settlementAnalysisContextRepository.save(
                SettlementAnalysisContext.createDefault(saved.getId())
        );

        return SettlementAnalysisSetResponse.from(saved);
    }

    public SettlementAnalysisSetItemResponse addUploadToSet(Long analysisSetId, Long uploadId) {
        SettlementAnalysisSet set = analysisSetRepository.findById(analysisSetId)
                .orElseThrow(() -> new AnalysisSetNotFoundException(analysisSetId));

        SettlementUpload upload = settlementUploadRepository.findById(uploadId)
                .orElseThrow(() -> new SettlementUploadNotFoundException(uploadId));

        boolean exists = analysisSetItemRepository.existsByAnalysisSetIdAndUploadId(set.getId(), upload.getId());
        if (exists) {
            throw new AnalysisSetUploadAlreadyExistsException(uploadId);
        }

        SettlementAnalysisSetItem saved = analysisSetItemRepository.save(
                SettlementAnalysisSetItem.create(
                        set.getId(),
                        upload.getId(),
                        upload.getFileType()
                )
        );

        return SettlementAnalysisSetItemResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<SettlementAnalysisSetResponse> getSets() {
        return analysisSetRepository.findAll().stream()
                .map(SettlementAnalysisSetResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SettlementAnalysisSetItemResponse> getSetItems(Long analysisSetId) {
        analysisSetRepository.findById(analysisSetId)
                .orElseThrow(() -> new AnalysisSetNotFoundException(analysisSetId));

        return analysisSetItemRepository.findAllByAnalysisSetIdOrderByIdAsc(analysisSetId).stream()
                .map(SettlementAnalysisSetItemResponse::from)
                .toList();
    }


    @Transactional
    public int backfillMissingContexts() {
        int createdCount = 0;

        for (SettlementAnalysisSet set : analysisSetRepository.findAll()) {
            boolean exists = settlementAnalysisContextRepository.existsByAnalysisSetId(set.getId());
            if (!exists) {
                settlementAnalysisContextRepository.save(
                        SettlementAnalysisContext.createDefault(set.getId())
                );
                createdCount++;
            }
        }

        return createdCount;
    }
    @Transactional(readOnly = true)
    public List<SettlementAnalysisSetResponse> getMySavedSets(Long userId) {
        Long resolvedUserId = resolveUserId(userId);

        if (resolvedUserId == null) {
            throw new ApiException(ErrorCode.AUTH_UNAUTHORIZED, "로그인이 필요합니다.");
        }

        return analysisSetRepository.findAllByUserIdOrderByCreatedAtDesc(resolvedUserId).stream()
                .map(SettlementAnalysisSetResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public SettlementAnalysisSetResponse getMySavedSet(Long analysisSetId, Long userId) {
        Long resolvedUserId = resolveUserId(userId);

        if (resolvedUserId == null) {
            throw new ApiException(ErrorCode.AUTH_UNAUTHORIZED, "로그인이 필요합니다.");
        }

        SettlementAnalysisSet set = analysisSetRepository.findByIdAndUserId(analysisSetId, resolvedUserId)
                .orElseThrow(() -> new ApiException(
                        ErrorCode.INVALID_INPUT_VALUE,
                        "저장된 분석 세트를 찾을 수 없습니다. analysisSetId=" + analysisSetId
                ));

        return SettlementAnalysisSetResponse.from(set);
    }

    @Transactional(readOnly = true)
    public void validateMySavedSetAccessible(Long analysisSetId, Long userId) {
        Long resolvedUserId = resolveUserId(userId);

        if (resolvedUserId == null) {
            throw new ApiException(ErrorCode.AUTH_UNAUTHORIZED, "로그인이 필요합니다.");
        }

        analysisSetRepository.findByIdAndUserId(analysisSetId, resolvedUserId)
                .orElseThrow(() -> new ApiException(
                        ErrorCode.INVALID_INPUT_VALUE,
                        "저장된 분석 세트를 찾을 수 없습니다. analysisSetId=" + analysisSetId
                ));
    }

    @Transactional(readOnly = true)
    public WorkspaceContextResponse getMySavedSetContext(Long analysisSetId, Long userId) {
        validateMySavedSetAccessible(analysisSetId, userId);

        SettlementAnalysisContext context = settlementAnalysisContextRepository.findByAnalysisSetId(analysisSetId)
                .orElseGet(() -> SettlementAnalysisContext.createDefault(analysisSetId));

        return WorkspaceContextResponse.from(context);
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