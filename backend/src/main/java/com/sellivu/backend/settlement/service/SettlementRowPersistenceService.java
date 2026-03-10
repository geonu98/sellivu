package com.sellivu.backend.settlement.service;

import com.sellivu.backend.settlement.domain.*;
import com.sellivu.backend.settlement.parser.SettlementParseResult;
import com.sellivu.backend.settlement.repository.SettlementDailyRowRepository;
import com.sellivu.backend.settlement.repository.SettlementFeeRowRepository;
import com.sellivu.backend.settlement.repository.SettlementOrderRowRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.stereotype.Service;

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
    public void persist(Long uploadId, SettlementParseResult parseResult) {
        switch (parseResult.getFileType()) {
            case DAILY_SETTLEMENT -> persistDailyRows(uploadId, parseResult);
            case ORDER_SETTLEMENT -> persistOrderRows(uploadId, parseResult);
            case FEE_DETAIL -> persistFeeRows(uploadId, parseResult);
        }
    }

    private void persistDailyRows(Long uploadId, SettlementParseResult parseResult) {
        List<SettlementDailyRow> rows = parseResult.getRows().stream()
                .map(row -> settlementDailyRowMapper.map(uploadId, row))
                .toList();

        settlementDailyRowRepository.saveAll(rows);
    }

    private void persistOrderRows(Long uploadId, SettlementParseResult parseResult) {
        List<SettlementOrderRow> rows = parseResult.getRows().stream()
                .map(row -> settlementOrderRowMapper.map(uploadId, row))
                .toList();

        settlementOrderRowRepository.saveAll(rows);
    }

    private void persistFeeRows(Long uploadId, SettlementParseResult parseResult) {
        List<SettlementFeeRow> rows = parseResult.getRows().stream()
                .map(row -> settlementFeeRowMapper.map(uploadId, row))
                .toList();

        settlementFeeRowRepository.saveAll(rows);
    }
}