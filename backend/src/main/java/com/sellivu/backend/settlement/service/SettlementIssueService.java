package com.sellivu.backend.settlement.service;

import com.sellivu.backend.settlement.domain.SettlementDailyRow;
import com.sellivu.backend.settlement.domain.SettlementIssue;
import com.sellivu.backend.settlement.domain.SettlementIssueType;
import com.sellivu.backend.settlement.domain.SettlementOrderSnapshot;
import com.sellivu.backend.settlement.dto.SettlementIssueResponse;
import com.sellivu.backend.settlement.repository.SettlementDailyRowRepository;
import com.sellivu.backend.settlement.repository.SettlementIssueRepository;
import com.sellivu.backend.settlement.repository.SettlementOrderSnapshotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SettlementIssueService {

    private static final List<SettlementIssueType> DAILY_ISSUE_TYPES = List.of(
            SettlementIssueType.DAILY_ROW_MISSING,
            SettlementIssueType.DAILY_ROW_DUPLICATED,
            SettlementIssueType.DAILY_AMOUNT_MISMATCH
    );

    private final SettlementIssueRepository issueRepository;
    private final SettlementOrderSnapshotRepository snapshotRepository;
    private final SettlementDailyRowRepository dailyRowRepository;

    @Transactional
    public void rebuildDailyIssues() {
        issueRepository.deleteAllByIssueTypeIn(DAILY_ISSUE_TYPES);

        List<SettlementOrderSnapshot> snapshots = snapshotRepository.findAll();
        List<SettlementDailyRow> dailyRows = dailyRowRepository.findAllByOrderByIdAsc();

        Map<LocalDate, List<SettlementOrderSnapshot>> snapshotsByDate = snapshots.stream()
                .filter(snapshot -> snapshot.getSettlementDate() != null)
                .collect(Collectors.groupingBy(SettlementOrderSnapshot::getSettlementDate));

        Map<LocalDate, List<SettlementDailyRow>> dailyRowsByDate = dailyRows.stream()
                .filter(row -> row.getSettlementCompletedDate() != null)
                .collect(Collectors.groupingBy(SettlementDailyRow::getSettlementCompletedDate));

        for (Map.Entry<LocalDate, List<SettlementOrderSnapshot>> entry : snapshotsByDate.entrySet()) {
            LocalDate settlementDate = entry.getKey();
            List<SettlementOrderSnapshot> sameDateSnapshots = entry.getValue();

            BigDecimal snapshotSum = sameDateSnapshots.stream()
                    .map(SettlementOrderSnapshot::getResolvedSettlementAmount)
                    .map(this::nullSafe)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            List<SettlementDailyRow> matchedDailyRows =
                    dailyRowsByDate.getOrDefault(settlementDate, Collections.emptyList());

            if (matchedDailyRows.isEmpty()) {
                issueRepository.save(SettlementIssue.create(
                        null,
                        SettlementIssueType.DAILY_ROW_MISSING,
                        null,
                        null,
                        "D:" + settlementDate,
                        "일별 정산 row 누락: settlementDate=" + settlementDate
                                + ", snapshotSum=" + printable(snapshotSum)
                ));
                continue;
            }

            if (matchedDailyRows.size() > 1) {
                BigDecimal dailySum = matchedDailyRows.stream()
                        .map(SettlementDailyRow::getSettlementAmount)
                        .map(this::nullSafe)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                issueRepository.save(SettlementIssue.create(
                        null,
                        SettlementIssueType.DAILY_ROW_DUPLICATED,
                        null,
                        null,
                        "D:" + settlementDate,
                        "일별 정산 row 중복: settlementDate=" + settlementDate
                                + ", dailyRowCount=" + matchedDailyRows.size()
                                + ", snapshotSum=" + printable(snapshotSum)
                                + ", dailySum=" + printable(dailySum)
                ));
                continue;
            }

            SettlementDailyRow dailyRow = matchedDailyRows.get(0);
            BigDecimal dailyAmount = nullSafe(dailyRow.getSettlementAmount());

            if (snapshotSum.compareTo(dailyAmount) != 0) {
                issueRepository.save(SettlementIssue.create(
                        null,
                        SettlementIssueType.DAILY_AMOUNT_MISMATCH,
                        null,
                        null,
                        "D:" + settlementDate,
                        "일별 정산 금액 불일치: settlementDate=" + settlementDate
                                + ", snapshotSum=" + printable(snapshotSum)
                                + ", dailyAmount=" + printable(dailyAmount)
                ));
            }
        }
    }

    @Transactional(readOnly = true)
    public List<SettlementIssueResponse> getAllIssues() {
        return issueRepository.findAllByOrderByIdDesc().stream()
                .map(SettlementIssueResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SettlementIssueResponse> getDailyIssues() {
        return issueRepository.findAllByIssueTypeInOrderByIdDesc(DAILY_ISSUE_TYPES).stream()
                .map(SettlementIssueResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SettlementIssueResponse> getIssuesBySnapshotId(Long snapshotId) {
        return issueRepository.findAllBySnapshotIdOrderByIdAsc(snapshotId).stream()
                .map(SettlementIssueResponse::from)
                .toList();
    }

    private BigDecimal nullSafe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String printable(BigDecimal value) {
        return value == null ? "null" : value.toPlainString();
    }
}