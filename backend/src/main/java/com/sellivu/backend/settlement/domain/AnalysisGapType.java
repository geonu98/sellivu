package com.sellivu.backend.settlement.domain;

public enum AnalysisGapType {
    DAILY_FILE_MISSING,
    ORDER_FILE_MISSING,
    FEE_FILE_MISSING,
    ORDER_FEE_CROSS_CHECK_UNAVAILABLE,
    DAILY_CROSS_CHECK_UNAVAILABLE
}