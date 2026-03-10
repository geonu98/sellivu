package com.sellivu.backend.settlement.repository;

import com.sellivu.backend.settlement.domain.SettlementIssue;
import com.sellivu.backend.settlement.domain.SettlementIssueType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
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
}