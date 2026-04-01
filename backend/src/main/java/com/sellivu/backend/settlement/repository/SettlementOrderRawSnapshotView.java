package com.sellivu.backend.settlement.repository;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface SettlementOrderRawSnapshotView {

    Long getId();

    Long getUploadId();

    String getJoinKey();

    String getOrderNo();

    String getProductOrderNo();

    String getProductName();

    LocalDate getPaymentDate();

    LocalDate getSettlementCompletedDate();

    BigDecimal getNpayFeeAmount();

    BigDecimal getSalesLinkedFeeTotal();

    BigDecimal getInstallmentFeeAmount();

    BigDecimal getBenefitAmount();

    BigDecimal getSettlementExpectedAmount();
}
