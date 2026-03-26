package com.sellivu.backend.settlement.service;

import com.sellivu.backend.settlement.domain.SettlementDailyRow;
import com.sellivu.backend.settlement.domain.SettlementFeeRow;
import com.sellivu.backend.settlement.domain.SettlementOrderRow;
import com.sellivu.backend.settlement.parser.SettlementParseResult;
import com.sellivu.backend.settlement.repository.SettlementDailyRowRepository;
import com.sellivu.backend.settlement.repository.SettlementFeeRowRepository;
import com.sellivu.backend.settlement.repository.SettlementOrderRowRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SettlementRowPersistenceService {

    private final SettlementDailyRowRepository settlementDailyRowRepository;
    private final SettlementOrderRowRepository settlementOrderRowRepository;
    private final SettlementFeeRowRepository settlementFeeRowRepository;
    private final SettlementDailyRowMapper settlementDailyRowMapper;
    private final SettlementOrderRowMapper settlementOrderRowMapper;
    private final SettlementFeeRowMapper settlementFeeRowMapper;

    public SettlementRowPersistenceService(
            SettlementDailyRowRepository settlementDailyRowRepository,
            SettlementOrderRowRepository settlementOrderRowRepository,
            SettlementFeeRowRepository settlementFeeRowRepository,
            SettlementDailyRowMapper settlementDailyRowMapper,
            SettlementOrderRowMapper settlementOrderRowMapper,
            SettlementFeeRowMapper settlementFeeRowMapper
    ) {
        this.settlementDailyRowRepository = settlementDailyRowRepository;
        this.settlementOrderRowRepository = settlementOrderRowRepository;
        this.settlementFeeRowRepository = settlementFeeRowRepository;
        this.settlementDailyRowMapper = settlementDailyRowMapper;
        this.settlementOrderRowMapper = settlementOrderRowMapper;
        this.settlementFeeRowMapper = settlementFeeRowMapper;
    }

    @Transactional
    public void persist(Long runId, Long uploadId, SettlementParseResult parseResult) {
        long totalStart = System.currentTimeMillis();
        try {
            switch (parseResult.getFileType()) {
                case DAILY_SETTLEMENT -> persistDailyRows(runId, uploadId, parseResult);
                case ORDER_SETTLEMENT -> persistOrderRows(uploadId, parseResult);
                case FEE_DETAIL -> persistFeeRows(uploadId, parseResult);
            }
        } finally {
            log.info("[PERF] persistRows.total fileType={} runId={} uploadId={} rowCount={} took={}ms",
                    parseResult.getFileType(),
                    runId,
                    uploadId,
                    parseResult.getRows().size(),
                    System.currentTimeMillis() - totalStart
            );
        }
    }

    private void persistDailyRows(Long runId, Long uploadId, SettlementParseResult parseResult) {
        long mapStart = System.currentTimeMillis();
        List<SettlementDailyRow> rows = parseResult.getRows().stream()
                .map(row -> settlementDailyRowMapper.map(runId, uploadId, row))
                .toList();
        log.info("[PERF] persistRows.mapDaily runId={} uploadId={} rows={} took={}ms",
                runId, uploadId, rows.size(), System.currentTimeMillis() - mapStart);

        long saveStart = System.currentTimeMillis();
        settlementDailyRowRepository.saveAll(rows);
        log.info("[PERF] persistRows.saveDaily runId={} uploadId={} rows={} took={}ms",
                runId, uploadId, rows.size(), System.currentTimeMillis() - saveStart);
    }

    private void persistOrderRows(Long uploadId, SettlementParseResult parseResult) {
        long mapStart = System.currentTimeMillis();
        List<SettlementOrderRow> rows = parseResult.getRows().stream()
                .map(row -> settlementOrderRowMapper.map(uploadId, row))
                .toList();
        log.info("[PERF] persistRows.mapOrder uploadId={} rows={} took={}ms",
                uploadId, rows.size(), System.currentTimeMillis() - mapStart);

        long saveStart = System.currentTimeMillis();
        settlementOrderRowRepository.saveAll(rows);
        log.info("[PERF] persistRows.saveOrder uploadId={} rows={} took={}ms",
                uploadId, rows.size(), System.currentTimeMillis() - saveStart);
    }

    private void persistFeeRows(Long uploadId, SettlementParseResult parseResult) {
        long mapStart = System.currentTimeMillis();
        List<SettlementFeeRow> rows = parseResult.getRows().stream()
                .map(row -> settlementFeeRowMapper.map(uploadId, row))
                .toList();
        log.info("[PERF] persistRows.mapFee uploadId={} rows={} took={}ms",
                uploadId, rows.size(), System.currentTimeMillis() - mapStart);

        long saveStart = System.currentTimeMillis();
        settlementFeeRowRepository.saveAll(rows);
        log.info("[PERF] persistRows.saveFee uploadId={} rows={} took={}ms",
                uploadId, rows.size(), System.currentTimeMillis() - saveStart);
    }
}