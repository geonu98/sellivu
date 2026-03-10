package com.sellivu.backend.settlement;

import com.sellivu.backend.settlement.parser.SettlementExcelParser;
import com.sellivu.backend.settlement.parser.SettlementParseResult;
import com.sellivu.backend.settlement.support.SettlementFileHashUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

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

    public record ParsedSettlementFile(
            String fileHash,
            SettlementParseResult parseResult
    ) {
    }
}