package com.sellivu.backend.settlement.repository;

import com.sellivu.backend.settlement.domain.SettlementDailyRow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SettlementDailyRowRepository extends JpaRepository<SettlementDailyRow, Long> {

    List<SettlementDailyRow> findAllByUploadId(Long uploadId);

    List<SettlementDailyRow> findAllByUploadIdOrderByIdAsc(Long uploadId);

    List<SettlementDailyRow> findAllByOrderByIdAsc();

    List<SettlementDailyRow> findAllByUploadIdInOrderBySettlementCompletedDateAscIdAsc(List<Long> uploadIds);
}