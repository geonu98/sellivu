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
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
public class SettlementUploadService {

    private final SettlementUploadRepository settlementUploadRepository;
    private final SettlementUploadStorage settlementUploadStorage;
    private final SettlementParseFacade settlementParseFacade;

    public SettlementUploadService(
            SettlementUploadRepository settlementUploadRepository,
            SettlementUploadStorage settlementUploadStorage,
            SettlementParseFacade settlementParseFacade
    ) {
        this.settlementUploadRepository = settlementUploadRepository;
        this.settlementUploadStorage = settlementUploadStorage;
        this.settlementParseFacade = settlementParseFacade;
    }

    @Transactional
    public SettlementUploadResponse upload(MultipartFile file) {
        long totalStart = System.currentTimeMillis();

        try {
            validateFile(file);

            long parseStart = System.currentTimeMillis();
            SettlementParseFacade.ParsedSettlementFile parsedFile =
                    settlementParseFacade.parse(file);
            log.info("[PERF] upload.parse originalFileName={} fileType={} took={}ms",
                    file.getOriginalFilename(),
                    parsedFile.parseResult().getFileType(),
                    System.currentTimeMillis() - parseStart
            );

            String fileHash = parsedFile.fileHash();

            long duplicateCheckStart = System.currentTimeMillis();
            settlementUploadRepository.findByFileHash(fileHash)
                    .ifPresent(existing -> {
                        throw new DuplicateSettlementUploadException(
                                "이미 분석된 파일입니다. uploadId=" + existing.getId()
                        );
                    });
            log.info("[PERF] upload.duplicateCheck fileType={} took={}ms",
                    parsedFile.parseResult().getFileType(),
                    System.currentTimeMillis() - duplicateCheckStart
            );

            long saveStart = System.currentTimeMillis();
            SettlementUpload upload = saveNewUpload(file, parsedFile);
            log.info("[PERF] upload.saveOnly fileType={} uploadId={} took={}ms",
                    parsedFile.parseResult().getFileType(),
                    upload.getId(),
                    System.currentTimeMillis() - saveStart
            );

            return SettlementUploadResponse.from(upload, "정산 파일 업로드가 완료되었습니다.");
        } finally {
            log.info("[PERF] upload.total originalFileName={} took={}ms",
                    file != null ? file.getOriginalFilename() : "null",
                    System.currentTimeMillis() - totalStart
            );
        }
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

        return saveNewUpload(file, parsedFile);
    }

    private SettlementUpload saveNewUpload(
            MultipartFile file,
            SettlementParseFacade.ParsedSettlementFile parsedFile
    ) {
        long totalStart = System.currentTimeMillis();

        long storeStart = System.currentTimeMillis();
        String storedFileName = settlementUploadStorage.store(file);
        log.info("[PERF] upload.store fileType={} took={}ms",
                parsedFile.parseResult().getFileType(),
                System.currentTimeMillis() - storeStart
        );

        SettlementUpload upload = SettlementUpload.uploaded(
                Objects.requireNonNullElse(file.getOriginalFilename(), "unknown"),
                storedFileName,
                parsedFile.fileHash(),
                parsedFile.parseResult().getFileType()
        );

        long saveUploadStart = System.currentTimeMillis();
        try {
            settlementUploadRepository.save(upload);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateSettlementUploadException("이미 분석된 파일입니다.");
        }
        log.info("[PERF] upload.saveUploadEntity fileType={} uploadId={} took={}ms",
                parsedFile.parseResult().getFileType(),
                upload.getId(),
                System.currentTimeMillis() - saveUploadStart
        );

        log.info("[PERF] upload.skipLegacyRowPersistence fileType={} uploadId={}",
                parsedFile.parseResult().getFileType(),
                upload.getId()
        );

        log.info("[PERF] upload.saveNewUpload.total fileType={} uploadId={} took={}ms",
                parsedFile.parseResult().getFileType(),
                upload.getId(),
                System.currentTimeMillis() - totalStart
        );

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