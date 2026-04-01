package com.sellivu.backend.settlement.repository;

import com.sellivu.backend.settlement.domain.SettlementFeeRaw;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SettlementFeeRawRepository extends JpaRepository<SettlementFeeRaw, Long> {

    List<SettlementFeeRaw> findAllByRunIdOrderByIdAsc(Long runId);

    List<SettlementFeeRawSnapshotView> findProjectedByRunIdOrderByIdAsc(Long runId);

    long countByRunId(Long runId);

    void deleteAllByRunId(Long runId);

    List<SettlementFeeRaw> findAllByRunId(Long runId);

    Page<SettlementFeeRaw> findAllByRunIdOrderByIdAsc(Long runId, Pageable pageable);
}
