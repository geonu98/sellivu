package com.sellivu.backend.settlement.repository;

import com.sellivu.backend.settlement.domain.SettlementFeeRaw;
import com.sellivu.backend.settlement.domain.SettlementOrderRaw;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SettlementOrderRawRepository extends JpaRepository<SettlementOrderRaw, Long> {

    List<SettlementOrderRaw> findAllByRunIdOrderByIdAsc(Long runId);

    List<SettlementOrderRawSnapshotView> findProjectedByRunIdOrderByIdAsc(Long runId);

    long countByRunId(Long runId);

    void deleteAllByRunId(Long runId);

    List<SettlementOrderRaw> findAllByRunId(Long runId);

    Page<SettlementOrderRaw> findAllByRunIdOrderByIdAsc(Long runId, Pageable pageable);


}
