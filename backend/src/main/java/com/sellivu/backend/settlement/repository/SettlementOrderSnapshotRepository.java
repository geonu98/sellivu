package com.sellivu.backend.settlement.repository;

import com.sellivu.backend.settlement.domain.MatchStatus;
import com.sellivu.backend.settlement.domain.SettlementOrderSnapshot;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementOrderSnapshotRepository extends JpaRepository<SettlementOrderSnapshot, Long> {

    Optional<SettlementOrderSnapshot> findByJoinKey(String joinKey);

    List<SettlementOrderSnapshot> findAllByOrderUploadIdOrFeeUploadId(Long orderUploadId, Long feeUploadId);

    List<SettlementOrderSnapshot> findAllByMatchStatusOrderByIdDesc(MatchStatus matchStatus);

    List<SettlementOrderSnapshot> findAllByOrderByIdDesc();

    List<SettlementOrderSnapshot> findAllByOrderUploadIdOrFeeUploadIdOrderByIdDesc(Long orderUploadId, Long feeUploadId);

    void deleteAllByOrderUploadIdOrFeeUploadId(Long orderUploadId, Long feeUploadId);

    List<SettlementOrderSnapshot> findAllByOrderUploadIdInOrFeeUploadIdInOrderByIdDesc(
            List<Long> orderUploadIds,
            List<Long> feeUploadIds
    );
}