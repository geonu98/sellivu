package com.sellivu.backend.settlement.repository;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface SettlementFeeRawSnapshotView {

    Long getId();

    Long getUploadId();

    String getJoinKey();

    String getOrderNo();

    String getProductOrderNo();

    String getProductName();

    LocalDate getSettlementCompletedDate();

    BigDecimal getFeeBaseAmount();

    BigDecimal getCommissionAmount();
}
