package com.sellivu.backend.settlement.service;

import com.sellivu.backend.settlement.SettlementParseFacade;
import com.sellivu.backend.settlement.domain.*;
import com.sellivu.backend.settlement.parser.SettlementParsedRow;
import com.sellivu.backend.settlement.repository.SettlementUploadRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SettlementRawLoadService {

    private static final int CHUNK_SIZE = 10000;

    private final SettlementUploadRepository settlementUploadRepository;
    private final SettlementUploadStorage settlementUploadStorage;
    private final SettlementParseFacade settlementParseFacade;
    private final SettlementOrderRawBatchWriter settlementOrderRawBatchWriter;
    private final SettlementFeeRawBatchWriter settlementFeeRawBatchWriter;
    private final SettlementDailyRowMapper settlementDailyRowMapper;
    private final SettlementDailyRawBatchWriter settlementDailyRawBatchWriter;

    public int loadOrderRaw(Long runId, Long uploadId) {
        long totalStart = System.currentTimeMillis();

        SettlementUpload upload = settlementUploadRepository.findById(uploadId)
                .orElseThrow(() -> new IllegalArgumentException("업로드 파일을 찾을 수 없습니다. uploadId=" + uploadId));

        if (upload.getFileType() != SettlementFileType.ORDER_SETTLEMENT) {
            throw new IllegalArgumentException("ORDER_SETTLEMENT 파일이 아닙니다. uploadId=" + uploadId);
        }

        long openStreamStart = System.currentTimeMillis();
        try (InputStream inputStream = settlementUploadStorage.openStream(upload.getStoredFileName())) {
            log.info(
                    "[PERF] rawLoad.openOrderStream runId={} uploadId={} took={}ms",
                    runId,
                    uploadId,
                    System.currentTimeMillis() - openStreamStart
            );

            long parseStart = System.currentTimeMillis();
            SettlementParseFacade.ParsedSettlementFile parsedFile =
                    settlementParseFacade.parse(upload.getOriginalFileName(), inputStream);
            log.info(
                    "[PERF] rawLoad.parseOrderFile runId={} uploadId={} rowCount={} took={}ms",
                    runId,
                    uploadId,
                    parsedFile.parseResult().getRows().size(),
                    System.currentTimeMillis() - parseStart
            );

            long castStart = System.currentTimeMillis();
            List<SettlementParsedRow> parsedRows = castParsedRows(parsedFile.parseResult().getRows());
            log.info(
                    "[PERF] rawLoad.castOrderRows runId={} uploadId={} rowCount={} took={}ms",
                    runId,
                    uploadId,
                    parsedRows.size(),
                    System.currentTimeMillis() - castStart
            );

            int totalInserted = 0;
            int chunkIndex = 0;

            for (int start = 0; start < parsedRows.size(); start += CHUNK_SIZE) {
                long chunkSliceStart = System.currentTimeMillis();
                int end = Math.min(start + CHUNK_SIZE, parsedRows.size());
                List<SettlementParsedRow> chunk = parsedRows.subList(start, end);
                log.info(
                        "[PERF] rawLoad.sliceOrderChunk runId={} uploadId={} chunk={} rows={} took={}ms",
                        runId,
                        uploadId,
                        chunkIndex,
                        chunk.size(),
                        System.currentTimeMillis() - chunkSliceStart
                );

                long mapStart = System.currentTimeMillis();
                log.info(
                        "[PERF] rawLoad.mapOrderChunk runId={} uploadId={} chunk={} rows={} took={}ms",
                        runId,
                        uploadId,
                        chunkIndex,
                        chunk.size(),
                        System.currentTimeMillis() - mapStart
                );

                long writeStart = System.currentTimeMillis();
                int inserted = settlementOrderRawBatchWriter.writeParsed(runId, uploadId, chunk);
                totalInserted += inserted;
                log.info(
                        "[PERF] rawLoad.writeOrderChunk runId={} uploadId={} chunk={} inserted={} took={}ms",
                        runId,
                        uploadId,
                        chunkIndex,
                        inserted,
                        System.currentTimeMillis() - writeStart
                );

                chunkIndex++;
            }

            log.info(
                    "[PERF] rawLoad.totalOrderRaw runId={} uploadId={} insertedCount={} took={}ms",
                    runId,
                    uploadId,
                    totalInserted,
                    System.currentTimeMillis() - totalStart
            );

            return totalInserted;
        } catch (Exception e) {
            throw new RuntimeException("ORDER raw 적재 중 오류가 발생했습니다. uploadId=" + uploadId, e);
        }
    }

    public int loadFeeRaw(Long runId, Long uploadId) {
        long totalStart = System.currentTimeMillis();

        SettlementUpload upload = settlementUploadRepository.findById(uploadId)
                .orElseThrow(() -> new IllegalArgumentException("업로드 파일을 찾을 수 없습니다. uploadId=" + uploadId));

        if (upload.getFileType() != SettlementFileType.FEE_DETAIL) {
            throw new IllegalArgumentException("FEE_DETAIL 파일이 아닙니다. uploadId=" + uploadId);
        }

        long openStreamStart = System.currentTimeMillis();
        try (InputStream inputStream = settlementUploadStorage.openStream(upload.getStoredFileName())) {
            log.info(
                    "[PERF] rawLoad.openFeeStream runId={} uploadId={} took={}ms",
                    runId,
                    uploadId,
                    System.currentTimeMillis() - openStreamStart
            );

            long parseStart = System.currentTimeMillis();
            SettlementParseFacade.ParsedSettlementFile parsedFile =
                    settlementParseFacade.parse(upload.getOriginalFileName(), inputStream);
            log.info(
                    "[PERF] rawLoad.parseFeeFile runId={} uploadId={} rowCount={} took={}ms",
                    runId,
                    uploadId,
                    parsedFile.parseResult().getRows().size(),
                    System.currentTimeMillis() - parseStart
            );

            long castStart = System.currentTimeMillis();
            List<SettlementParsedRow> parsedRows = castParsedRows(parsedFile.parseResult().getRows());
            log.info(
                    "[PERF] rawLoad.castFeeRows runId={} uploadId={} rowCount={} took={}ms",
                    runId,
                    uploadId,
                    parsedRows.size(),
                    System.currentTimeMillis() - castStart
            );

            int totalInserted = 0;
            int chunkIndex = 0;

            for (int start = 0; start < parsedRows.size(); start += CHUNK_SIZE) {
                long chunkSliceStart = System.currentTimeMillis();
                int end = Math.min(start + CHUNK_SIZE, parsedRows.size());
                List<SettlementParsedRow> chunk = parsedRows.subList(start, end);
                log.info(
                        "[PERF] rawLoad.sliceFeeChunk runId={} uploadId={} chunk={} rows={} took={}ms",
                        runId,
                        uploadId,
                        chunkIndex,
                        chunk.size(),
                        System.currentTimeMillis() - chunkSliceStart
                );

                long mapStart = System.currentTimeMillis();
                log.info(
                        "[PERF] rawLoad.mapFeeChunk runId={} uploadId={} chunk={} rows={} took={}ms",
                        runId,
                        uploadId,
                        chunkIndex,
                        chunk.size(),
                        System.currentTimeMillis() - mapStart
                );

                long writeStart = System.currentTimeMillis();
                int inserted = settlementFeeRawBatchWriter.writeParsed(runId, uploadId, chunk);
                totalInserted += inserted;
                log.info(
                        "[PERF] rawLoad.writeFeeChunk runId={} uploadId={} chunk={} inserted={} took={}ms",
                        runId,
                        uploadId,
                        chunkIndex,
                        inserted,
                        System.currentTimeMillis() - writeStart
                );

                chunkIndex++;
            }

            log.info(
                    "[PERF] rawLoad.totalFeeRaw runId={} uploadId={} insertedCount={} took={}ms",
                    runId,
                    uploadId,
                    totalInserted,
                    System.currentTimeMillis() - totalStart
            );

            return totalInserted;
        } catch (Exception e) {
            throw new RuntimeException("FEE raw 적재 중 오류가 발생했습니다. uploadId=" + uploadId, e);
        }
    }

    @SuppressWarnings("unchecked")
    private List<SettlementParsedRow> castParsedRows(List<?> rows) {
        return (List<SettlementParsedRow>) rows;
    }

    @Transactional
    public int loadDailyRaw(Long runId, Long uploadId) {
        long totalStart = System.currentTimeMillis();

        SettlementUpload upload = settlementUploadRepository.findById(uploadId)
                .orElseThrow(() -> new IllegalArgumentException("업로드 파일을 찾을 수 없습니다. uploadId=" + uploadId));

        if (upload.getFileType() != SettlementFileType.DAILY_SETTLEMENT) {
            throw new IllegalArgumentException("DAILY_SETTLEMENT 파일이 아닙니다. uploadId=" + uploadId);
        }

        long openStreamStart = System.currentTimeMillis();
        try (InputStream inputStream = settlementUploadStorage.openStream(upload.getStoredFileName())) {
            log.info(
                    "[PERF] rawLoad.openDailyStream runId={} uploadId={} took={}ms",
                    runId,
                    uploadId,
                    System.currentTimeMillis() - openStreamStart
            );

            long parseStart = System.currentTimeMillis();
            SettlementParseFacade.ParsedSettlementFile parsedFile =
                    settlementParseFacade.parse(upload.getOriginalFileName(), inputStream);

            log.info(
                    "[PERF] rawLoad.parseDailyFile runId={} uploadId={} rowCount={} took={}ms",
                    runId,
                    uploadId,
                    parsedFile.parseResult().getRows().size(),
                    System.currentTimeMillis() - parseStart
            );

            long castStart = System.currentTimeMillis();
            List<SettlementParsedRow> parsedRows = castParsedRows(parsedFile.parseResult().getRows());
            log.info(
                    "[PERF] rawLoad.castDailyRows runId={} uploadId={} rowCount={} took={}ms",
                    runId,
                    uploadId,
                    parsedRows.size(),
                    System.currentTimeMillis() - castStart
            );

            int totalInserted = 0;
            int chunkIndex = 0;

            for (int start = 0; start < parsedRows.size(); start += CHUNK_SIZE) {
                long chunkSliceStart = System.currentTimeMillis();
                int end = Math.min(start + CHUNK_SIZE, parsedRows.size());
                List<SettlementParsedRow> chunk = parsedRows.subList(start, end);
                log.info(
                        "[PERF] rawLoad.sliceDailyChunk runId={} uploadId={} chunk={} rows={} took={}ms",
                        runId,
                        uploadId,
                        chunkIndex,
                        chunk.size(),
                        System.currentTimeMillis() - chunkSliceStart
                );

                long mapStart = System.currentTimeMillis();
                List<SettlementDailyRow> rows = new ArrayList<>(chunk.size());
                for (SettlementParsedRow parsedRow : chunk) {
                    rows.add(settlementDailyRowMapper.map(runId, uploadId, parsedRow));
                }

                log.info(
                        "[PERF] rawLoad.mapDailyChunk runId={} uploadId={} chunk={} rows={} took={}ms",
                        runId,
                        uploadId,
                        chunkIndex,
                        rows.size(),
                        System.currentTimeMillis() - mapStart
                );

                long writeStart = System.currentTimeMillis();
                int inserted = settlementDailyRawBatchWriter.write(runId, rows);
                totalInserted += inserted;

                log.info(
                        "[PERF] rawLoad.writeDailyChunk runId={} uploadId={} chunk={} inserted={} took={}ms",
                        runId,
                        uploadId,
                        chunkIndex,
                        inserted,
                        System.currentTimeMillis() - writeStart
                );

                chunkIndex++;
            }

            log.info(
                    "[PERF] rawLoad.totalDailyRaw runId={} uploadId={} insertedCount={} took={}ms",
                    runId,
                    uploadId,
                    totalInserted,
                    System.currentTimeMillis() - totalStart
            );

            return totalInserted;
        } catch (Exception e) {
            throw new RuntimeException("DAILY raw 적재 중 오류가 발생했습니다. uploadId=" + uploadId, e);
        }
    }
}
