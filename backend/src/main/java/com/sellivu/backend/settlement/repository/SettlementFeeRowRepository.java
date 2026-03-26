package com.sellivu.backend.settlement.repository;

import com.sellivu.backend.settlement.domain.SettlementFeeRow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SettlementFeeRowRepository extends JpaRepository<SettlementFeeRow, Long> {

    List<SettlementFeeRow> findAllByUploadId(Long uploadId);

    List<SettlementFeeRow> findAllByProductOrderNo(String productOrderNo);

    List<SettlementFeeRow> findAllByOrderNo(String orderNo);

    List<SettlementFeeRow> findAllByUploadIdOrderByIdAsc(Long uploadId);

    List<SettlementFeeRow> findAllByOrderByIdAsc();

    List<SettlementFeeRow> findAllByUploadIdInOrderBySettlementCompletedDateAscIdAsc(List<Long> uploadIds);

    List<SettlementFeeRow> findAllByProductOrderNoIn(List<String> productOrderNos);

    List<SettlementFeeRow> findAllByOrderNoIn(List<String> orderNos);

    List<SettlementFeeRow> findAllByUploadIdOrderByRowNoAsc(Long uploadId);

    Page<SettlementFeeRow> findByUploadIdOrderByRowNoAsc(Long uploadId, Pageable pageable);
}