package com.sellivu.backend.settlement.repository;

import com.sellivu.backend.settlement.domain.SettlementUpload;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SettlementUploadRepository extends JpaRepository<SettlementUpload, Long> {

    Optional<SettlementUpload> findByFileHash(String fileHash);

    List<SettlementUpload> findAllByOrderByIdAsc();

    List<SettlementUpload> findAllByIdInOrderByIdAsc(List<Long> ids);
}