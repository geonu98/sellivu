package com.sellivu.backend.settlement.repository;

import com.sellivu.backend.settlement.domain.SettlementOrderRow;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementOrderRowRepository extends JpaRepository<SettlementOrderRow, Long> {

    List<SettlementOrderRow> findAllByUploadId(Long uploadId);

    List<SettlementOrderRow> findAllByProductOrderNo(String productOrderNo);

    List<SettlementOrderRow> findAllByOrderNo(String orderNo);

    List<SettlementOrderRow> findAllByUploadIdOrderByIdAsc(Long uploadId);

    List<SettlementOrderRow> findAllByOrderByIdAsc();

    List<SettlementOrderRow> findAllByUploadIdInOrderBySettlementCompletedDateAscIdAsc(List<Long> uploadIds);
}