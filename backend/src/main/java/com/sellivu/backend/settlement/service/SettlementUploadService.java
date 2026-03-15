package com.sellivu.backend.settlement.service;

import com.sellivu.backend.settlement.SettlementParseFacade;
import com.sellivu.backend.settlement.domain.SettlementUpload;
import com.sellivu.backend.settlement.dto.SettlementUploadResponse;
import com.sellivu.backend.settlement.exception.DuplicateSettlementUploadException;
import com.sellivu.backend.settlement.repository.SettlementUploadRepository;
import jakarta.transaction.Transactional;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class SettlementUploadService {

    private final SettlementUploadRepository settlementUploadRepository;
    private final SettlementUploadStorage settlementUploadStorage;
    private final SettlementParseFacade settlementParseFacade;
    private final SettlementRowPersistenceService settlementRowPersistenceService;
    private final SettlementOrderSnapshotService settlementOrderSnapshotService;

    public SettlementUploadService(
            SettlementUploadRepository settlementUploadRepository,
            SettlementUploadStorage settlementUploadStorage,
            SettlementParseFacade settlementParseFacade,
            SettlementRowPersistenceService settlementRowPersistenceService,
            SettlementOrderSnapshotService settlementOrderSnapshotService
    ) {
        this.settlementUploadRepository = settlementUploadRepository;
        this.settlementUploadStorage = settlementUploadStorage;
        this.settlementParseFacade = settlementParseFacade;
        this.settlementRowPersistenceService = settlementRowPersistenceService;
        this.settlementOrderSnapshotService = settlementOrderSnapshotService;
    }

    @Transactional
    public SettlementUploadResponse upload(MultipartFile file) {
        validateFile(file);

        SettlementParseFacade.ParsedSettlementFile parsedFile =
                settlementParseFacade.parse(file);

        String fileHash = parsedFile.fileHash();

        settlementUploadRepository.findByFileHash(fileHash)
                .ifPresent(existing -> {
                    throw new DuplicateSettlementUploadException(
                            "이미 분석된 파일입니다. uploadId=" + existing.getId()
                    );
                });

        SettlementUpload upload = saveAndParseNewUpload(file, parsedFile);

        return SettlementUploadResponse.from(upload, "정산 파일 업로드 및 파싱이 완료되었습니다.");
    }

    @Transactional
    public SettlementUpload getOrCreateUpload(MultipartFile file) {
        validateFile(file);

        SettlementParseFacade.ParsedSettlementFile parsedFile =
                settlementParseFacade.parse(file);

        String fileHash = parsedFile.fileHash();

        Optional<SettlementUpload> existing = settlementUploadRepository.findByFileHash(fileHash);
        if (existing.isPresent()) {
            return existing.get();
        }

        return saveAndParseNewUpload(file, parsedFile);
    }

    private SettlementUpload saveAndParseNewUpload(
            MultipartFile file,
            SettlementParseFacade.ParsedSettlementFile parsedFile
    ) {
        String storedFileName = settlementUploadStorage.store(file);

        SettlementUpload upload = SettlementUpload.uploaded(
                Objects.requireNonNullElse(file.getOriginalFilename(), "unknown"),
                storedFileName,
                parsedFile.fileHash(),
                parsedFile.parseResult().getFileType()
        );

        try {
            settlementUploadRepository.save(upload);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateSettlementUploadException("이미 분석된 파일입니다.");
        }

        try {
            upload.markParsing();

            settlementRowPersistenceService.persist(upload.getId(), parsedFile.parseResult());

            settlementOrderSnapshotService.aggregateForUpload(
                    upload.getId(),
                    parsedFile.parseResult().getFileType()
            );

            upload.markParsed();
        } catch (Exception e) {
            upload.markFailed(e.getMessage());
            throw e;
        }

        return upload;
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 비어 있습니다.");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !isSupportedFile(originalFilename)) {
            throw new IllegalArgumentException("엑셀(xlsx, xls) 또는 csv 파일만 업로드할 수 있습니다.");
        }
    }

    private boolean isSupportedFile(String originalFilename) {
        String lower = originalFilename.toLowerCase(Locale.ROOT);
        return lower.endsWith(".xlsx") || lower.endsWith(".xls") || lower.endsWith(".csv");
    }
}