package com.sellivu.backend.settlement;

import com.sellivu.backend.settlement.parser.SettlementExcelParser;
import com.sellivu.backend.settlement.parser.SettlementParseResult;
import com.sellivu.backend.settlement.support.SettlementFileHashUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@Component
public class SettlementParseFacade {

    private final SettlementExcelParser settlementExcelParser;

    public SettlementParseFacade(SettlementExcelParser settlementExcelParser) {
        this.settlementExcelParser = settlementExcelParser;
    }

    public ParsedSettlementFile parse(MultipartFile file) {
        String fileHash = SettlementFileHashUtils.sha256(file);
        SettlementParseResult parseResult = settlementExcelParser.parse(file);
        return new ParsedSettlementFile(fileHash, parseResult);
    }

    public ParsedSettlementFile parse(String originalFileName, InputStream inputStream) {
        try {
            byte[] bytes = inputStream.readAllBytes();

            MultipartFile file = new InMemoryMultipartFile(
                    "file",
                    originalFileName,
                    "application/octet-stream",
                    bytes
            );

            String fileHash = SettlementFileHashUtils.sha256(file);
            SettlementParseResult parseResult = settlementExcelParser.parse(file);
            return new ParsedSettlementFile(fileHash, parseResult);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "정산 파일 재파싱 중 오류가 발생했습니다. originalFileName=" + originalFileName,
                    e
            );
        }
    }

    public record ParsedSettlementFile(
            String fileHash,
            SettlementParseResult parseResult
    ) {
    }

    private static final class InMemoryMultipartFile implements MultipartFile {

        private final String name;
        private final String originalFilename;
        private final String contentType;
        private final byte[] content;

        private InMemoryMultipartFile(
                String name,
                String originalFilename,
                String contentType,
                byte[] content
        ) {
            this.name = name;
            this.originalFilename = originalFilename;
            this.contentType = contentType;
            this.content = content == null ? new byte[0] : content;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getOriginalFilename() {
            return originalFilename;
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public boolean isEmpty() {
            return content.length == 0;
        }

        @Override
        public long getSize() {
            return content.length;
        }

        @Override
        public byte[] getBytes() {
            return content;
        }

        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream(content);
        }

        @Override
        public void transferTo(java.io.File dest) throws IOException, IllegalStateException {
            java.nio.file.Files.write(dest.toPath(), content);
        }
    }
}