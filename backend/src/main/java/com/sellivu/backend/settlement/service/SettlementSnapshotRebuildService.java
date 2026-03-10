package com.sellivu.backend.settlement.service;

import com.sellivu.backend.settlement.domain.SettlementFileType;
import com.sellivu.backend.settlement.domain.SettlementOrderSnapshot;
import com.sellivu.backend.settlement.domain.SettlementUpload;
import com.sellivu.backend.settlement.exception.SettlementSnapshotNotFoundException;
import com.sellivu.backend.settlement.exception.SettlementUploadNotFoundException;
import com.sellivu.backend.settlement.repository.SettlementFeeRowRepository;
import com.sellivu.backend.settlement.repository.SettlementIssueRepository;
import com.sellivu.backend.settlement.repository.SettlementOrderRowRepository;
import com.sellivu.backend.settlement.repository.SettlementOrderSnapshotRepository;
import com.sellivu.backend.settlement.repository.SettlementUploadRepository;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SettlementSnapshotRebuildService {

    private final SettlementUploadRepository settlementUploadRepository;
    private final SettlementOrderRowRepository orderRowRepository;
    private final SettlementFeeRowRepository feeRowRepository;
    private final SettlementOrderSnapshotRepository snapshotRepository;
    private final SettlementIssueRepository issueRepository;
    private final SettlementOrderSnapshotService settlementOrderSnapshotService;
    private final SettlementIssueService settlementIssueService;

    @Transactional
    public int rebuildAll() {
        clearAllSnapshotsAndIssues();

        int rebuildCount = 0;
        List<SettlementUpload> uploads = settlementUploadRepository.findAllByOrderByIdAsc();

        for (SettlementUpload upload : uploads) {
            SettlementFileType fileType = upload.getFileType();
            if (fileType == SettlementFileType.ORDER_SETTLEMENT || fileType == SettlementFileType.FEE_DETAIL) {
                settlementOrderSnapshotService.aggregateForUpload(upload.getId(), fileType);
                rebuildCount++;
            }
        }

        settlementIssueService.rebuildDailyIssues();
        return rebuildCount;
    }

    @Transactional
    public int rebuildByUpload(Long uploadId) {
        SettlementUpload upload = settlementUploadRepository.findById(uploadId)
                .orElseThrow(() -> new SettlementUploadNotFoundException(uploadId));

        SettlementFileType fileType = upload.getFileType();
        if (fileType != SettlementFileType.ORDER_SETTLEMENT && fileType != SettlementFileType.FEE_DETAIL) {
            return 0;
        }

        List<SettlementOrderSnapshot> affectedSnapshots =
                snapshotRepository.findAllByOrderUploadIdOrFeeUploadId(uploadId, uploadId);

        for (SettlementOrderSnapshot snapshot : affectedSnapshots) {
            issueRepository.deleteAllBySnapshotId(snapshot.getId());
        }
        snapshotRepository.deleteAll(affectedSnapshots);

        settlementOrderSnapshotService.aggregateForUpload(uploadId, fileType);
        settlementIssueService.rebuildDailyIssues();
        return 1;
    }

    @Transactional
    public void rebuildSnapshot(Long snapshotId) {
        SettlementOrderSnapshot snapshot = snapshotRepository.findById(snapshotId)
                .orElseThrow(() -> new SettlementSnapshotNotFoundException(snapshotId));

        String productOrderNo = snapshot.getProductOrderNo();
        String orderNo = snapshot.getOrderNo();

        issueRepository.deleteAllBySnapshotId(snapshotId);
        snapshotRepository.delete(snapshot);

        Set<Long> rebuiltOrderRowIds = new HashSet<>();
        Set<Long> rebuiltFeeRowIds = new HashSet<>();

        if (productOrderNo != null && !productOrderNo.isBlank()) {
            orderRowRepository.findAllByProductOrderNo(productOrderNo)
                    .forEach(orderRow -> {
                        settlementOrderSnapshotService.aggregateByOrderRow(orderRow);
                        rebuiltOrderRowIds.add(orderRow.getId());
                    });

            feeRowRepository.findAllByProductOrderNo(productOrderNo)
                    .forEach(feeRow -> {
                        settlementOrderSnapshotService.aggregateByFeeRow(feeRow);
                        rebuiltFeeRowIds.add(feeRow.getId());
                    });
        }

        if (orderNo != null && !orderNo.isBlank()) {
            orderRowRepository.findAllByOrderNo(orderNo)
                    .forEach(orderRow -> {
                        if (!rebuiltOrderRowIds.contains(orderRow.getId())) {
                            settlementOrderSnapshotService.aggregateByOrderRow(orderRow);
                            rebuiltOrderRowIds.add(orderRow.getId());
                        }
                    });

            feeRowRepository.findAllByOrderNo(orderNo)
                    .forEach(feeRow -> {
                        if (!rebuiltFeeRowIds.contains(feeRow.getId())) {
                            settlementOrderSnapshotService.aggregateByFeeRow(feeRow);
                            rebuiltFeeRowIds.add(feeRow.getId());
                        }
                    });
        }

        settlementIssueService.rebuildDailyIssues();
    }

    @Transactional(readOnly = true)
    public List<Long> findRebuildableUploadIds() {
        List<Long> result = new ArrayList<>();

        for (SettlementUpload upload : settlementUploadRepository.findAllByOrderByIdAsc()) {
            if (upload.getFileType() == SettlementFileType.ORDER_SETTLEMENT
                    || upload.getFileType() == SettlementFileType.FEE_DETAIL) {
                result.add(upload.getId());
            }
        }

        return result;
    }

    private void clearAllSnapshotsAndIssues() {
        List<SettlementOrderSnapshot> snapshots = snapshotRepository.findAll();

        for (SettlementOrderSnapshot snapshot : snapshots) {
            issueRepository.deleteAllBySnapshotId(snapshot.getId());
        }

        issueRepository.deleteAllByIssueTypeIn(List.of(
                com.sellivu.backend.settlement.domain.SettlementIssueType.DAILY_ROW_MISSING,
                com.sellivu.backend.settlement.domain.SettlementIssueType.DAILY_ROW_DUPLICATED,
                com.sellivu.backend.settlement.domain.SettlementIssueType.DAILY_AMOUNT_MISMATCH
        ));

        snapshotRepository.deleteAll();
    }
}