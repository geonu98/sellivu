package com.sellivu.backend.settlement.repository;

import com.sellivu.backend.settlement.domain.SettlementDailyRaw;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SettlementDailyRawRepository extends JpaRepository<SettlementDailyRaw, Long> {

    Page<SettlementDailyRaw> findAllByRunIdOrderByIdAsc(Long runId, Pageable pageable);

    long countByRunId(Long runId);

    List<SettlementDailyRaw> findAllByRunIdOrderBySettlementCompletedDateAscIdAsc(Long runId);
}