package com.sellivu.backend.settlement.repository;

import com.sellivu.backend.settlement.domain.MatchStatus;
import com.sellivu.backend.settlement.domain.SettlementOrderSnapshot;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SettlementOrderSnapshotRepository extends JpaRepository<SettlementOrderSnapshot, Long> {

    Optional<SettlementOrderSnapshot> findByJoinKey(String joinKey);

    List<SettlementOrderSnapshot> findAllByJoinKeyIn(List<String> joinKeys);

    List<SettlementOrderSnapshot> findAllByOrderUploadIdOrFeeUploadId(Long orderUploadId, Long feeUploadId);

    List<SettlementOrderSnapshot> findAllByMatchStatusOrderByIdDesc(MatchStatus matchStatus);

    List<SettlementOrderSnapshot> findAllByOrderByIdDesc();

    List<SettlementOrderSnapshot> findAllByOrderUploadIdOrFeeUploadIdOrderByIdDesc(Long orderUploadId, Long feeUploadId);

    void deleteAllByOrderUploadIdOrFeeUploadId(Long orderUploadId, Long feeUploadId);

    List<SettlementOrderSnapshot> findAllByOrderUploadIdInOrFeeUploadIdInOrderByIdDesc(
            List<Long> orderUploadIds,
            List<Long> feeUploadIds
    );

    Optional<SettlementOrderSnapshot> findByRunIdAndJoinKey(Long runId, String joinKey);

    List<SettlementOrderSnapshot> findAllByRunIdAndJoinKeyIn(Long runId, List<String> joinKeys);

    List<SettlementOrderSnapshot> findAllByRunIdOrderByIdAsc(Long runId);

    List<SettlementOrderSnapshot> findAllByRunIdOrderByIdDesc(Long runId);

    long countByRunId(Long runId);

    void deleteAllByRunId(Long runId);

    Page<SettlementOrderSnapshot> findAllByRunIdOrderByIdDesc(Long runId, Pageable pageable);

    @Query("""
        select coalesce(sum(s.issueCount), 0)
        from SettlementOrderSnapshot s
        where s.runId = :runId
    """)
    long sumIssueCountByRunId(@Param("runId") Long runId);
}