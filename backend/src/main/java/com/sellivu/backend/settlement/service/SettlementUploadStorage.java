package com.sellivu.backend.settlement.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class SettlementUploadStorage {

    private final Path uploadRootPath;

    public SettlementUploadStorage(
            @Value("${app.settlement.upload-dir:uploads/settlement}") String uploadDir
    ) {
        this.uploadRootPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        createDirectoryIfNeeded();
    }

    public String store(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String extension = extractExtension(originalFilename);
        String storedFileName = UUID.randomUUID() + extension;

        Path targetPath = uploadRootPath.resolve(storedFileName);

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            return storedFileName;
        } catch (IOException e) {
            throw new IllegalStateException("정산 파일 저장 중 오류가 발생했습니다.", e);
        }
    }

    private void createDirectoryIfNeeded() {
        try {
            Files.createDirectories(uploadRootPath);
        } catch (IOException e) {
            throw new IllegalStateException("정산 업로드 디렉토리 생성 중 오류가 발생했습니다.", e);
        }
    }

    private String extractExtension(String filename) {
        if (filename == null || filename.isBlank()) {
            return "";
        }
        int index = filename.lastIndexOf('.');
        if (index < 0) {
            return "";
        }
        return filename.substring(index);
    }
}