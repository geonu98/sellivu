package com.sellivu.backend.settlement.service;

import com.sellivu.backend.settlement.domain.MatchStatus;
import com.sellivu.backend.settlement.domain.SettlementFeeRaw;
import com.sellivu.backend.settlement.domain.SettlementOrderRaw;
import com.sellivu.backend.settlement.domain.SettlementOrderSnapshot;
import com.sellivu.backend.settlement.repository.SettlementFeeRawRepository;
import com.sellivu.backend.settlement.repository.SettlementOrderRawRepository;
import com.sellivu.backend.settlement.repository.SettlementOrderSnapshotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SettlementSnapshotBuildService {

    private static final long ISSUE_ORDER_ONLY = 1L << 0;
    private static final long ISSUE_FEE_ONLY = 1L << 1;
    private static final long ISSUE_SETTLEMENT_MISMATCH = 1L << 2;
    private static final long ISSUE_COMMISSION_MISMATCH = 1L << 3;
    private static final long ISSUE_NET_MISMATCH = 1L << 4;
    private static final long ISSUE_REFUND_CANDIDATE = 1L << 5;
    private static final long ISSUE_NEEDS_USER_INPUT = 1L << 6;

    private final SettlementOrderRawRepository settlementOrderRawRepository;
    private final SettlementFeeRawRepository settlementFeeRawRepository;
    private final SettlementOrderSnapshotRepository settlementOrderSnapshotRepository;
    private final SettlementOrderSnapshotBatchWriter settlementOrderSnapshotBatchWriter;

    public int build(Long runId) {
        long totalStartedAt = System.currentTimeMillis();

        settlementOrderSnapshotRepository.deleteAllByRunId(runId);

        long loadStartedAt = System.currentTimeMillis();
        List<SettlementOrderRaw> orderRows = settlementOrderRawRepository.findAllByRunIdOrderByIdAsc(runId);
        List<SettlementFeeRaw> feeRows = settlementFeeRawRepository.findAllByRunIdOrderByIdAsc(runId);
        log.info("[PERF] snapshot raw load runId={} orderRows={} feeRows={} took={}ms",
                runId, orderRows.size(), feeRows.size(), System.currentTimeMillis() - loadStartedAt);

        long groupingStartedAt = System.currentTimeMillis();

        Map<String, List<SettlementOrderRaw>> orderMap = new LinkedHashMap<>();
        for (SettlementOrderRaw row : orderRows) {
            orderMap.computeIfAbsent(row.getJoinKey(), k -> new ArrayList<>()).add(row);
        }

        Map<String, List<SettlementFeeRaw>> feeMap = new LinkedHashMap<>();
        for (SettlementFeeRaw row : feeRows) {
            feeMap.computeIfAbsent(row.getJoinKey(), k -> new ArrayList<>()).add(row);
        }

        Set<String> joinKeys = new LinkedHashSet<>();
        joinKeys.addAll(orderMap.keySet());
        joinKeys.addAll(feeMap.keySet());

        log.info("[PERF] snapshot grouping runId={} joinKeys={} took={}ms",
                runId, joinKeys.size(), System.currentTimeMillis() - groupingStartedAt);

        long buildStartedAt = System.currentTimeMillis();
        List<SettlementOrderSnapshot> snapshots = new ArrayList<>(joinKeys.size());

        for (String joinKey : joinKeys) {
            List<SettlementOrderRaw> orders = orderMap.getOrDefault(joinKey, Collections.emptyList());
            List<SettlementFeeRaw> fees = feeMap.getOrDefault(joinKey, Collections.emptyList());

            SettlementOrderRaw representativeOrder = orders.isEmpty() ? null : orders.get(0);
            SettlementFeeRaw representativeFee = fees.isEmpty() ? null : fees.get(0);

            MatchStatus matchStatus = resolveMatchStatus(orders, fees);

            BigDecimal orderSettlementAmount = sumOrderSettlementAmount(orders);
            BigDecimal orderCommissionAmount = sumOrderCommissionAmount(orders);
            BigDecimal orderNetAmount = orderSettlementAmount.subtract(orderCommissionAmount);

            BigDecimal feeSettlementAmount = sumFeeSettlementAmount(fees);
            BigDecimal feeCommissionAmount = sumFeeCommissionAmount(fees);
            BigDecimal feeNetAmount = feeSettlementAmount.subtract(feeCommissionAmount);

            BigDecimal resolvedSettlementAmount = orderSettlementAmount;
            BigDecimal resolvedCommissionAmount = orderCommissionAmount;
            BigDecimal resolvedNetAmount = orderNetAmount;

            boolean settlementAmountMatched = orderSettlementAmount.compareTo(feeSettlementAmount) == 0;
            boolean commissionAmountMatched = orderCommissionAmount.compareTo(feeCommissionAmount) == 0;
            boolean netAmountMatched = orderNetAmount.compareTo(feeNetAmount) == 0;

            SettlementOrderSnapshot snapshot = SettlementOrderSnapshot.create(
                    runId,
                    joinKey,
                    representativeOrder != null ? representativeOrder.getOrderNo()
                            : representativeFee != null ? representativeFee.getOrderNo() : null,
                    representativeOrder != null ? representativeOrder.getProductOrderNo()
                            : representativeFee != null ? representativeFee.getProductOrderNo() : null,
                    matchStatus,
                    representativeOrder != null ? representativeOrder.getId() : null,
                    representativeFee != null ? representativeFee.getId() : null,
                    representativeOrder != null ? representativeOrder.getUploadId() : null,
                    representativeFee != null ? representativeFee.getUploadId() : null,
                    representativeOrder != null ? representativeOrder.getProductName()
                            : representativeFee != null ? representativeFee.getProductName() : null,
                    null,
                    null,
                    null,
                    resolvePaidAt(representativeOrder),
                    resolveSettlementDate(representativeOrder, representativeFee),
                    orderSettlementAmount,
                    orderCommissionAmount,
                    orderNetAmount,
                    feeSettlementAmount,
                    feeCommissionAmount,
                    feeNetAmount,
                    resolvedSettlementAmount,
                    resolvedCommissionAmount,
                    resolvedNetAmount,
                    settlementAmountMatched,
                    commissionAmountMatched,
                    netAmountMatched,
                    0
            );

            IssueSummary issueSummary = buildIssueSummary(
                    matchStatus,
                    orderSettlementAmount,
                    feeSettlementAmount,
                    orderCommissionAmount,
                    feeCommissionAmount,
                    orderNetAmount,
                    feeNetAmount
            );

            snapshot.updateIssueSummary(
                    issueSummary.issueMask(),
                    issueSummary.issueCount(),
                    issueSummary.primaryIssueCode(),
                    null,
                    issueSummary.refundCandidate(),
                    issueSummary.needsUserInput()
            );

            snapshots.add(snapshot);
        }

        log.info("[PERF] snapshot entity build runId={} snapshots={} took={}ms",
                runId, snapshots.size(), System.currentTimeMillis() - buildStartedAt);

        long saveStartedAt = System.currentTimeMillis();
        settlementOrderSnapshotBatchWriter.insertBatch(snapshots);
        log.info("[PERF] snapshot batch insert runId={} snapshots={} took={}ms",
                runId, snapshots.size(), System.currentTimeMillis() - saveStartedAt);

        log.info("[PERF] snapshot total runId={} total={}ms",
                runId, System.currentTimeMillis() - totalStartedAt);

        return snapshots.size();
    }

    private IssueSummary buildIssueSummary(
            MatchStatus matchStatus,
            BigDecimal orderSettlementAmount,
            BigDecimal feeSettlementAmount,
            BigDecimal orderCommissionAmount,
            BigDecimal feeCommissionAmount,
            BigDecimal orderNetAmount,
            BigDecimal feeNetAmount
    ) {
        long issueMask = 0L;
        int issueCount = 0;
        String primaryIssueCode = null;
        boolean refundCandidate = false;
        boolean needsUserInput = false;

        if (matchStatus == MatchStatus.ORDER_ONLY) {
            issueMask |= ISSUE_ORDER_ONLY;
            issueCount++;
            primaryIssueCode = firstPrimary(primaryIssueCode, "ORDER_ONLY");
            needsUserInput = true;
        }

        if (matchStatus == MatchStatus.FEE_ONLY) {
            issueMask |= ISSUE_FEE_ONLY;
            issueCount++;
            primaryIssueCode = firstPrimary(primaryIssueCode, "FEE_ONLY");
            needsUserInput = true;
        }

        if (orderSettlementAmount.compareTo(feeSettlementAmount) != 0) {
            issueMask |= ISSUE_SETTLEMENT_MISMATCH;
            issueCount++;
            primaryIssueCode = firstPrimary(primaryIssueCode, "SETTLEMENT_MISMATCH");
        }

        if (orderCommissionAmount.compareTo(feeCommissionAmount) != 0) {
            BigDecimal diff = orderCommissionAmount.subtract(feeCommissionAmount);
            issueMask |= ISSUE_COMMISSION_MISMATCH;
            issueCount++;
            primaryIssueCode = firstPrimary(primaryIssueCode, "COMMISSION_MISMATCH");

            if (diff.compareTo(BigDecimal.ZERO) < 0) {
                issueMask |= ISSUE_REFUND_CANDIDATE;
                refundCandidate = true;
            }
        }

        if (orderNetAmount.compareTo(feeNetAmount) != 0) {
            issueMask |= ISSUE_NET_MISMATCH;
            issueCount++;
            primaryIssueCode = firstPrimary(primaryIssueCode, "NET_MISMATCH");
        }

        if (refundCandidate) {
            issueMask |= ISSUE_REFUND_CANDIDATE;
        }

        if (needsUserInput) {
            issueMask |= ISSUE_NEEDS_USER_INPUT;
        }

        return new IssueSummary(
                issueMask,
                issueCount,
                primaryIssueCode,
                refundCandidate,
                needsUserInput
        );
    }

    private MatchStatus resolveMatchStatus(List<SettlementOrderRaw> orders, List<SettlementFeeRaw> fees) {
        if (!orders.isEmpty() && !fees.isEmpty()) return MatchStatus.MATCHED;
        if (!orders.isEmpty()) return MatchStatus.ORDER_ONLY;
        return MatchStatus.FEE_ONLY;
    }

    private BigDecimal sumOrderSettlementAmount(List<SettlementOrderRaw> rows) {
        return rows.stream()
                .map(SettlementOrderRaw::getSettlementExpectedAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumOrderCommissionAmount(List<SettlementOrderRaw> rows) {
        return rows.stream()
                .map(this::orderCommissionOf)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal orderCommissionOf(SettlementOrderRaw row) {
        return nvl(row.getNpayFeeAmount())
                .add(nvl(row.getSalesLinkedFeeTotal()))
                .add(nvl(row.getInstallmentFeeAmount()))
                .subtract(nvl(row.getBenefitAmount()));
    }

    private BigDecimal sumFeeSettlementAmount(List<SettlementFeeRaw> rows) {
        return rows.stream()
                .map(SettlementFeeRaw::getFeeBaseAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumFeeCommissionAmount(List<SettlementFeeRaw> rows) {
        return rows.stream()
                .map(SettlementFeeRaw::getCommissionAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private LocalDate resolvePaidAt(SettlementOrderRaw order) {
        return order != null ? order.getPaymentDate() : null;
    }

    private LocalDate resolveSettlementDate(SettlementOrderRaw order, SettlementFeeRaw fee) {
        if (order != null && order.getSettlementCompletedDate() != null) {
            return order.getSettlementCompletedDate();
        }
        if (fee != null) {
            return fee.getSettlementCompletedDate();
        }
        return null;
    }

    private String firstPrimary(String current, String candidate) {
        return current == null ? candidate : current;
    }

    private BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private record IssueSummary(
            long issueMask,
            int issueCount,
            String primaryIssueCode,
            boolean refundCandidate,
            boolean needsUserInput
    ) {}
}