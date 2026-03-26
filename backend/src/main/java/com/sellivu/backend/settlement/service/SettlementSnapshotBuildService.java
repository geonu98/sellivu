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
                runId,
                orderRows.size(),
                feeRows.size(),
                System.currentTimeMillis() - loadStartedAt
        );

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
                runId,
                joinKeys.size(),
                System.currentTimeMillis() - groupingStartedAt
        );

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

            snapshots.add(snapshot);
        }

        log.info("[PERF] snapshot entity build runId={} snapshots={} took={}ms",
                runId,
                snapshots.size(),
                System.currentTimeMillis() - buildStartedAt
        );

        long saveStartedAt = System.currentTimeMillis();
        settlementOrderSnapshotBatchWriter.insertBatch(snapshots);
        log.info("[PERF] snapshot batch insert runId={} snapshots={} took={}ms",
                runId,
                snapshots.size(),
                System.currentTimeMillis() - saveStartedAt
        );

        log.info("[PERF] snapshot total runId={} total={}ms",
                runId,
                System.currentTimeMillis() - totalStartedAt
        );

        return snapshots.size();
    }

    private MatchStatus resolveMatchStatus(List<SettlementOrderRaw> orders, List<SettlementFeeRaw> fees) {
        if (!orders.isEmpty() && !fees.isEmpty()) {
            return MatchStatus.MATCHED;
        }
        if (!orders.isEmpty()) {
            return MatchStatus.ORDER_ONLY;
        }
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

    private BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}