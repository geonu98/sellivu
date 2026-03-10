package com.sellivu.backend.settlement.repository;

import com.sellivu.backend.settlement.domain.SettlementFeeRow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SettlementFeeRowRepository extends JpaRepository<SettlementFeeRow, Long> {

    List<SettlementFeeRow> findAllByUploadId(Long uploadId);

    List<SettlementFeeRow> findAllByProductOrderNo(String productOrderNo);

    List<SettlementFeeRow> findAllByOrderNo(String orderNo);

    List<SettlementFeeRow> findAllByUploadIdOrderByIdAsc(Long uploadId);

    List<SettlementFeeRow> findAllByOrderByIdAsc();

    List<SettlementFeeRow> findAllByUploadIdInOrderBySettlementCompletedDateAscIdAsc(List<Long> uploadIds);
}