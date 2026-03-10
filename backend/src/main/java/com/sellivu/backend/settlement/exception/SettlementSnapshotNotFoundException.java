package com.sellivu.backend.settlement.exception;

import com.sellivu.backend.global.error.ApiException;
import org.springframework.http.HttpStatus;

public class SettlementSnapshotNotFoundException extends ApiException {

    public SettlementSnapshotNotFoundException(Long snapshotId) {
        super(
                HttpStatus.NOT_FOUND,
                "SETTLEMENT_SNAPSHOT_NOT_FOUND",
                "해당 snapshot을 찾을 수 없습니다. id=" + snapshotId
        );
    }
}