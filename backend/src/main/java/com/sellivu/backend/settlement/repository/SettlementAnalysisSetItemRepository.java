package com.sellivu.backend.settlement.repository;

import com.sellivu.backend.settlement.domain.SettlementAnalysisSetItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SettlementAnalysisSetItemRepository extends JpaRepository<SettlementAnalysisSetItem, Long> {

    List<SettlementAnalysisSetItem> findAllByAnalysisSetIdOrderByIdAsc(Long analysisSetId);

    boolean existsByAnalysisSetIdAndUploadId(Long analysisSetId, Long uploadId);

    Optional<SettlementAnalysisSetItem> findByAnalysisSetIdAndUploadId(Long analysisSetId, Long uploadId);

    void deleteAllByAnalysisSetId(Long analysisSetId);
}