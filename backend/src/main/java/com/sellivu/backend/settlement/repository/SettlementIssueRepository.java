package com.sellivu.backend.settlement.repository;

import com.sellivu.backend.settlement.domain.SettlementIssue;
import com.sellivu.backend.settlement.domain.SettlementIssueType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SettlementIssueRepository extends JpaRepository<SettlementIssue, Long> {

    List<SettlementIssue> findAllBySnapshotId(Long snapshotId);

    List<SettlementIssue> findAllBySnapshotIdOrderByIdAsc(Long snapshotId);

    void deleteAllBySnapshotId(Long snapshotId);

    void deleteAllByIssueTypeIn(List<SettlementIssueType> issueTypes);

    List<SettlementIssue> findAllByIssueTypeInOrderByIdDesc(List<SettlementIssueType> issueTypes);

    List<SettlementIssue> findAllByOrderByIdDesc();

    List<SettlementIssue> findAllBySnapshotIdInOrderByIdDesc(List<Long> snapshotIds);

    List<SettlementIssue> findAllByJoinKeyStartingWithOrderByIdDesc(String joinKeyPrefix);

    void deleteAllByJoinKeyStartingWith(String joinKeyPrefix);



    List<SettlementIssue> findAllByRunIdOrderByIdDesc(Long runId);

    long countByRunId(Long runId);

    void deleteAllByRunId(Long runId);

    List<SettlementIssue> findAllByRunIdAndIssueTypeInOrderByIdDesc(
            Long runId,
            List<SettlementIssueType> issueTypes
    );

    Page<SettlementIssue> findAllByRunIdOrderByIdDesc(Long runId, Pageable pageable);
}