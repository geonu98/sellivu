package com.sellivu.backend.settlement.repository;

import com.sellivu.backend.settlement.domain.SettlementOrderRow;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementOrderRowRepository extends JpaRepository<SettlementOrderRow, Long> {

    List<SettlementOrderRow> findAllByUploadId(Long uploadId);

    List<SettlementOrderRow> findAllByProductOrderNo(String productOrderNo);

    List<SettlementOrderRow> findAllByOrderNo(String orderNo);

    List<SettlementOrderRow> findAllByUploadIdOrderByIdAsc(Long uploadId);

    List<SettlementOrderRow> findAllByOrderByIdAsc();

    List<SettlementOrderRow> findAllByUploadIdInOrderBySettlementCompletedDateAscIdAsc(List<Long> uploadIds);

    List<SettlementOrderRow> findAllByProductOrderNoIn(List<String> productOrderNos);

    List<SettlementOrderRow> findAllByOrderNoIn(List<String> orderNos);

    List<SettlementOrderRow> findAllByUploadIdOrderByRowNoAsc(Long uploadId);

    Page<SettlementOrderRow> findByUploadIdOrderByRowNoAsc(Long uploadId, Pageable pageable);
}