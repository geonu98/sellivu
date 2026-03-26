package com.sellivu.backend.settlement.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(staticName = "of")
public class SettlementRunStartResponse {
    private final Long runId;
}