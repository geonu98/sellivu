package com.sellivu.backend.settlement.service;

import com.sellivu.backend.settlement.domain.MatchStatus;
import com.sellivu.backend.settlement.domain.SettlementFeeRow;
import com.sellivu.backend.settlement.domain.SettlementFileType;
import com.sellivu.backend.settlement.domain.SettlementIssue;
import com.sellivu.backend.settlement.domain.SettlementIssueType;
import com.sellivu.backend.settlement.domain.SettlementOrderRow;
import com.sellivu.backend.settlement.domain.SettlementOrderSnapshot;
import com.sellivu.backend.settlement.repository.SettlementFeeRowRepository;
import com.sellivu.backend.settlement.repository.SettlementIssueRepository;
import com.sellivu.backend.settlement.repository.SettlementOrderRowRepository;
import com.sellivu.backend.settlement.repository.SettlementOrderSnapshotRepository;
import com.sellivu.backend.settlement.util.SettlementJoinKeyResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementOrderSnapshotService {

    private final SettlementOrderRowRepository orderRowRepository;
    private final SettlementFeeRowRepository feeRowRepository;
    private final SettlementOrderSnapshotRepository snapshotRepository;
    private final SettlementIssueRepository issueRepository;

    @Transactional
    public void aggregateForUpload(Long uploadId, SettlementFileType fileType) {
        long totalStart = System.currentTimeMillis();

        if (fileType == SettlementFileType.ORDER_SETTLEMENT) {
            long loadStart = System.currentTimeMillis();
            List<SettlementOrderRow> uploadedOrderRows = orderRowRepository.findAllByUploadId(uploadId);
            log.info("[PERF] snapshot.loadOrderRows uploadId={} rows={} took={}ms",
                    uploadId,
                    uploadedOrderRows.size(),
                    System.currentTimeMillis() - loadStart
            );

            long collectStart = System.currentTimeMillis();
            Set<String> distinctJoinKeys = new LinkedHashSet<>();
            List<String> productOrderNos = new ArrayList<>();
            List<String> orderNos = new ArrayList<>();
            int blankJoinKeyCount = 0;

            for (SettlementOrderRow orderRow : uploadedOrderRows) {
                String joinKey = SettlementJoinKeyResolver.resolve(
                        orderRow.getProductOrderNo(),
                        orderRow.getOrderNo()
                );
                if (isBlank(joinKey)) {
                    blankJoinKeyCount++;
                    continue;
                }

                if (distinctJoinKeys.add(joinKey)) {
                    if (joinKey.startsWith("P:")) {
                        productOrderNos.add(joinKey.substring(2));
                    } else if (joinKey.startsWith("O:")) {
                        orderNos.add(joinKey.substring(2));
                    }
                }
            }

            log.info("[PERF] snapshot.collectOrderJoinKeys uploadId={} orderRows={} distinctJoinKeys={} blankJoinKeys={} took={}ms",
                    uploadId,
                    uploadedOrderRows.size(),
                    distinctJoinKeys.size(),
                    blankJoinKeyCount,
                    System.currentTimeMillis() - collectStart
            );

            long preloadStart = System.currentTimeMillis();

            List<SettlementOrderRow> preloadedOrderRows = new ArrayList<>();
            if (!productOrderNos.isEmpty()) {
                preloadedOrderRows.addAll(orderRowRepository.findAllByProductOrderNoIn(productOrderNos));
            }
            if (!orderNos.isEmpty()) {
                preloadedOrderRows.addAll(orderRowRepository.findAllByOrderNoIn(orderNos));
            }

            List<SettlementFeeRow> preloadedFeeRows = new ArrayList<>();
            if (!productOrderNos.isEmpty()) {
                preloadedFeeRows.addAll(feeRowRepository.findAllByProductOrderNoIn(productOrderNos));
            }
            if (!orderNos.isEmpty()) {
                preloadedFeeRows.addAll(feeRowRepository.findAllByOrderNoIn(orderNos));
            }

            List<SettlementOrderSnapshot> preloadedSnapshots =
                    distinctJoinKeys.isEmpty()
                            ? List.of()
                            : snapshotRepository.findAllByJoinKeyIn(new ArrayList<>(distinctJoinKeys));

            log.info("[PERF] snapshot.preloadOrder uploadId={} orderRows={} feeRows={} snapshots={} took={}ms",
                    uploadId,
                    preloadedOrderRows.size(),
                    preloadedFeeRows.size(),
                    preloadedSnapshots.size(),
                    System.currentTimeMillis() - preloadStart
            );

            long mapStart = System.currentTimeMillis();

            Map<String, List<SettlementOrderRow>> orderRowsByJoinKey = buildOrderRowsByJoinKey(preloadedOrderRows);
            Map<String, List<SettlementFeeRow>> feeRowsByJoinKey = buildFeeRowsByJoinKey(preloadedFeeRows);
            Map<String, SettlementOrderSnapshot> snapshotByJoinKey = buildSnapshotByJoinKey(preloadedSnapshots);

            log.info("[PERF] snapshot.buildOrderMaps uploadId={} orderJoinKeys={} feeJoinKeys={} snapshotJoinKeys={} took={}ms",
                    uploadId,
                    orderRowsByJoinKey.size(),
                    feeRowsByJoinKey.size(),
                    snapshotByJoinKey.size(),
                    System.currentTimeMillis() - mapStart
            );

            long aggregateStart = System.currentTimeMillis();
            for (String joinKey : distinctJoinKeys) {
                rebuildSnapshotByJoinKey(
                        joinKey,
                        orderRowsByJoinKey.getOrDefault(joinKey, List.of()),
                        feeRowsByJoinKey.getOrDefault(joinKey, List.of()),
                        snapshotByJoinKey.get(joinKey)
                );
            }

            log.info("[PERF] snapshot.aggregateOrderDistinctJoinKeys uploadId={} distinctJoinKeys={} took={}ms",
                    uploadId,
                    distinctJoinKeys.size(),
                    System.currentTimeMillis() - aggregateStart
            );

            log.info("[PERF] snapshot.total uploadId={} fileType={} took={}ms",
                    uploadId,
                    fileType,
                    System.currentTimeMillis() - totalStart
            );
            return;
        }

        if (fileType == SettlementFileType.FEE_DETAIL) {
            long loadStart = System.currentTimeMillis();
            List<SettlementFeeRow> uploadedFeeRows = feeRowRepository.findAllByUploadId(uploadId);
            log.info("[PERF] snapshot.loadFeeRows uploadId={} rows={} took={}ms",
                    uploadId,
                    uploadedFeeRows.size(),
                    System.currentTimeMillis() - loadStart
            );

            long collectStart = System.currentTimeMillis();
            Set<String> distinctJoinKeys = new LinkedHashSet<>();
            List<String> productOrderNos = new ArrayList<>();
            List<String> orderNos = new ArrayList<>();
            int blankJoinKeyCount = 0;

            for (SettlementFeeRow feeRow : uploadedFeeRows) {
                String joinKey = SettlementJoinKeyResolver.resolve(
                        feeRow.getProductOrderNo(),
                        feeRow.getOrderNo()
                );
                if (isBlank(joinKey)) {
                    blankJoinKeyCount++;
                    continue;
                }

                if (distinctJoinKeys.add(joinKey)) {
                    if (joinKey.startsWith("P:")) {
                        productOrderNos.add(joinKey.substring(2));
                    } else if (joinKey.startsWith("O:")) {
                        orderNos.add(joinKey.substring(2));
                    }
                }
            }

            log.info("[PERF] snapshot.collectFeeJoinKeys uploadId={} feeRows={} distinctJoinKeys={} blankJoinKeys={} took={}ms",
                    uploadId,
                    uploadedFeeRows.size(),
                    distinctJoinKeys.size(),
                    blankJoinKeyCount,
                    System.currentTimeMillis() - collectStart
            );

            long preloadStart = System.currentTimeMillis();

            List<SettlementOrderRow> preloadedOrderRows = new ArrayList<>();
            if (!productOrderNos.isEmpty()) {
                preloadedOrderRows.addAll(orderRowRepository.findAllByProductOrderNoIn(productOrderNos));
            }
            if (!orderNos.isEmpty()) {
                preloadedOrderRows.addAll(orderRowRepository.findAllByOrderNoIn(orderNos));
            }

            List<SettlementFeeRow> preloadedFeeRows = new ArrayList<>();
            if (!productOrderNos.isEmpty()) {
                preloadedFeeRows.addAll(feeRowRepository.findAllByProductOrderNoIn(productOrderNos));
            }
            if (!orderNos.isEmpty()) {
                preloadedFeeRows.addAll(feeRowRepository.findAllByOrderNoIn(orderNos));
            }

            List<SettlementOrderSnapshot> preloadedSnapshots =
                    distinctJoinKeys.isEmpty()
                            ? List.of()
                            : snapshotRepository.findAllByJoinKeyIn(new ArrayList<>(distinctJoinKeys));

            log.info("[PERF] snapshot.preloadFee uploadId={} orderRows={} feeRows={} snapshots={} took={}ms",
                    uploadId,
                    preloadedOrderRows.size(),
                    preloadedFeeRows.size(),
                    preloadedSnapshots.size(),
                    System.currentTimeMillis() - preloadStart
            );

            long mapStart = System.currentTimeMillis();

            Map<String, List<SettlementOrderRow>> orderRowsByJoinKey = buildOrderRowsByJoinKey(preloadedOrderRows);
            Map<String, List<SettlementFeeRow>> feeRowsByJoinKey = buildFeeRowsByJoinKey(preloadedFeeRows);
            Map<String, SettlementOrderSnapshot> snapshotByJoinKey = buildSnapshotByJoinKey(preloadedSnapshots);

            log.info("[PERF] snapshot.buildFeeMaps uploadId={} orderJoinKeys={} feeJoinKeys={} snapshotJoinKeys={} took={}ms",
                    uploadId,
                    orderRowsByJoinKey.size(),
                    feeRowsByJoinKey.size(),
                    snapshotByJoinKey.size(),
                    System.currentTimeMillis() - mapStart
            );

            long aggregateStart = System.currentTimeMillis();
            for (String joinKey : distinctJoinKeys) {
                rebuildSnapshotByJoinKey(
                        joinKey,
                        orderRowsByJoinKey.getOrDefault(joinKey, List.of()),
                        feeRowsByJoinKey.getOrDefault(joinKey, List.of()),
                        snapshotByJoinKey.get(joinKey)
                );
            }

            log.info("[PERF] snapshot.aggregateFeeDistinctJoinKeys uploadId={} distinctJoinKeys={} took={}ms",
                    uploadId,
                    distinctJoinKeys.size(),
                    System.currentTimeMillis() - aggregateStart
            );

            log.info("[PERF] snapshot.total uploadId={} fileType={} took={}ms",
                    uploadId,
                    fileType,
                    System.currentTimeMillis() - totalStart
            );
        }
    }

    @Transactional
    public void aggregateByOrderRow(SettlementOrderRow orderRow) {
        String joinKey = SettlementJoinKeyResolver.resolve(
                orderRow.getProductOrderNo(),
                orderRow.getOrderNo()
        );
        if (isBlank(joinKey)) {
            createStandaloneIssue(
                    SettlementIssueType.MISSING_JOIN_KEY,
                    orderRow.getOrderNo(),
                    orderRow.getProductOrderNo(),
                    null,
                    "건별 정산 row에 ORDER_NO / PRODUCT_ORDER_NO가 모두 없어 병합할 수 없습니다."
            );
            return;
        }

        rebuildSnapshotByJoinKey(joinKey);
    }

    @Transactional
    public void aggregateByFeeRow(SettlementFeeRow feeRow) {
        String joinKey = SettlementJoinKeyResolver.resolve(
                feeRow.getProductOrderNo(),
                feeRow.getOrderNo()
        );
        if (isBlank(joinKey)) {
            createStandaloneIssue(
                    SettlementIssueType.MISSING_JOIN_KEY,
                    feeRow.getOrderNo(),
                    feeRow.getProductOrderNo(),
                    null,
                    "수수료 상세 row에 ORDER_NO / PRODUCT_ORDER_NO가 모두 없어 병합할 수 없습니다."
            );
            return;
        }

        rebuildSnapshotByJoinKey(joinKey);
    }

    private void rebuildSnapshotByJoinKey(String joinKey) {
        long totalStart = System.currentTimeMillis();

        long loadOrderStart = System.currentTimeMillis();
        List<SettlementOrderRow> orderRows = findOrderRowsByJoinKey(joinKey);
        long loadOrderTook = System.currentTimeMillis() - loadOrderStart;

        long loadFeeStart = System.currentTimeMillis();
        List<SettlementFeeRow> feeRows = findFeeRowsByJoinKey(joinKey);
        long loadFeeTook = System.currentTimeMillis() - loadFeeStart;

        if (orderRows.isEmpty() && feeRows.isEmpty()) {
            log.info("[PERF] snapshot.rebuild joinKey={} orderRows=0 feeRows=0 loadOrder={}ms loadFee={}ms total={}ms",
                    joinKey,
                    loadOrderTook,
                    loadFeeTook,
                    System.currentTimeMillis() - totalStart
            );
            return;
        }

        long snapshotLoadStart = System.currentTimeMillis();
        SettlementOrderSnapshot existingSnapshot = snapshotRepository.findByJoinKey(joinKey).orElse(null);
        long snapshotLoadTook = System.currentTimeMillis() - snapshotLoadStart;

        long rebuildStart = System.currentTimeMillis();
        rebuildSnapshotByJoinKey(joinKey, orderRows, feeRows, existingSnapshot);
        long rebuildTook = System.currentTimeMillis() - rebuildStart;

        long totalTook = System.currentTimeMillis() - totalStart;

        if (totalTook > 300) {
            log.info("[PERF] snapshot.rebuild joinKey={} orderRows={} feeRows={} loadOrder={}ms loadFee={}ms snapshotLoad={}ms rebuild={}ms total={}ms",
                    joinKey,
                    orderRows.size(),
                    feeRows.size(),
                    loadOrderTook,
                    loadFeeTook,
                    snapshotLoadTook,
                    rebuildTook,
                    totalTook
            );
        }
    }

    private void rebuildSnapshotByJoinKey(
            String joinKey,
            List<SettlementOrderRow> orderRows,
            List<SettlementFeeRow> feeRows,
            SettlementOrderSnapshot existingSnapshot
    ) {
        long totalStart = System.currentTimeMillis();

        if (orderRows.isEmpty() && feeRows.isEmpty()) {
            log.info("[PERF] snapshot.rebuild.cached joinKey={} orderRows=0 feeRows=0 total={}ms",
                    joinKey,
                    System.currentTimeMillis() - totalStart
            );
            return;
        }

        long aggregateAmountStart = System.currentTimeMillis();

        SettlementOrderRow representativeOrderRow = chooseRepresentativeOrderRow(orderRows).orElse(null);
        SettlementFeeRow representativeFeeRow = chooseRepresentativeFeeRow(feeRows).orElse(null);

        BigDecimal orderSettlementAmount = aggregateOrderSettlementAmount(orderRows);
        BigDecimal orderCommissionAmount = aggregateOrderCommissionAmount(orderRows);
        BigDecimal orderNetAmount = aggregateOrderNetAmount(orderRows);
        BigDecimal orderBenefitAmount = aggregateOrderBenefitAmount(orderRows);

        BigDecimal feeSettlementAmount = aggregateFeeSettlementAmount(feeRows);
        BigDecimal feeCommissionAmount = aggregateFeeCommissionAmount(feeRows);
        BigDecimal feeNetAmount = aggregateFeeNetAmount(feeSettlementAmount, feeCommissionAmount, feeRows);

        boolean settlementMatched = compareNullable(orderSettlementAmount, feeSettlementAmount);
        boolean commissionMatched = compareNullable(orderCommissionAmount, feeCommissionAmount);
        boolean netMatched = compareNullable(orderNetAmount, feeNetAmount);

        boolean explainableNetDifference = isExplainableNetDifference(
                representativeOrderRow,
                orderNetAmount,
                feeNetAmount,
                orderBenefitAmount
        );

        MatchStatus matchStatus = resolveMatchStatus(
                orderRows,
                feeRows,
                settlementMatched,
                commissionMatched,
                netMatched,
                explainableNetDifference
        );

        BigDecimal resolvedSettlementAmount = preferMatchedValue(orderSettlementAmount, feeSettlementAmount);
        BigDecimal resolvedCommissionAmount = preferMatchedValue(orderCommissionAmount, feeCommissionAmount);
        BigDecimal resolvedNetAmount = orderNetAmount != null
                ? orderNetAmount
                : preferMatchedValue(orderNetAmount, feeNetAmount);

        String orderNo = firstNonBlank(
                representativeOrderRow != null ? representativeOrderRow.getOrderNo() : null,
                representativeFeeRow != null ? representativeFeeRow.getOrderNo() : null
        );
        String productOrderNo = firstNonBlank(
                representativeOrderRow != null ? representativeOrderRow.getProductOrderNo() : null,
                representativeFeeRow != null ? representativeFeeRow.getProductOrderNo() : null
        );
        String productName = firstNonBlank(
                representativeOrderRow != null ? representativeOrderRow.getProductName() : null,
                representativeFeeRow != null ? representativeFeeRow.getProductName() : null
        );

        String optionName = null;
        String sellerProductCode = null;
        String sellerOptionCode = null;

        LocalDate paidAt = aggregatePaidAt(orderRows);
        LocalDate settlementDate = firstNonNull(
                aggregateOrderSettlementDate(orderRows),
                aggregateFeeSettlementDate(feeRows)
        );

        long aggregateAmountTook = System.currentTimeMillis() - aggregateAmountStart;

        long issueBuildStart = System.currentTimeMillis();
        List<SettlementIssue> issues = buildIssues(
                representativeOrderRow,
                joinKey,
                orderNo,
                productOrderNo,
                matchStatus,
                settlementMatched,
                commissionMatched,
                netMatched,
                explainableNetDifference,
                orderSettlementAmount,
                feeSettlementAmount,
                orderCommissionAmount,
                feeCommissionAmount,
                orderNetAmount,
                feeNetAmount
        );
        long issueBuildTook = System.currentTimeMillis() - issueBuildStart;

        SettlementOrderSnapshot snapshot = existingSnapshot != null
                ? existingSnapshot
                : SettlementOrderSnapshot.create(
                null, //일단 null  runid 없어서
                joinKey,
                orderNo,
                productOrderNo,
                matchStatus,
                representativeOrderRow != null ? representativeOrderRow.getId() : null,
                representativeFeeRow != null ? representativeFeeRow.getId() : null,
                representativeOrderRow != null ? representativeOrderRow.getUploadId() : null,
                representativeFeeRow != null ? representativeFeeRow.getUploadId() : null,
                productName,
                optionName,
                sellerProductCode,
                sellerOptionCode,
                paidAt,
                settlementDate,
                orderSettlementAmount,
                orderCommissionAmount,
                orderNetAmount,
                feeSettlementAmount,
                feeCommissionAmount,
                feeNetAmount,
                resolvedSettlementAmount,
                resolvedCommissionAmount,
                resolvedNetAmount,
                settlementMatched,
                commissionMatched,
                netMatched || explainableNetDifference,
                issues.size()
        );

        if (snapshot.getId() != null) {
            snapshot.update(
                    matchStatus,
                    representativeOrderRow != null ? representativeOrderRow.getId() : null,
                    representativeFeeRow != null ? representativeFeeRow.getId() : null,
                    representativeOrderRow != null ? representativeOrderRow.getUploadId() : null,
                    representativeFeeRow != null ? representativeFeeRow.getUploadId() : null,
                    productName,
                    optionName,
                    sellerProductCode,
                    sellerOptionCode,
                    paidAt,
                    settlementDate,
                    orderSettlementAmount,
                    orderCommissionAmount,
                    orderNetAmount,
                    feeSettlementAmount,
                    feeCommissionAmount,
                    feeNetAmount,
                    resolvedSettlementAmount,
                    resolvedCommissionAmount,
                    resolvedNetAmount,
                    settlementMatched,
                    commissionMatched,
                    netMatched || explainableNetDifference,
                    issues.size()
            );
        }

        long snapshotSaveStart = System.currentTimeMillis();
        SettlementOrderSnapshot saved = snapshotRepository.save(snapshot);
        long snapshotSaveTook = System.currentTimeMillis() - snapshotSaveStart;

        long issueDeleteStart = System.currentTimeMillis();
        issueRepository.deleteAllBySnapshotId(saved.getId());
        long issueDeleteTook = System.currentTimeMillis() - issueDeleteStart;

        long issueSaveStart = System.currentTimeMillis();
        for (SettlementIssue issue : issues) {
            issueRepository.save(
                    SettlementIssue.create(
                            saved.getId(),
                            issue.getIssueType(),
                            orderNo,
                            productOrderNo,
                            joinKey,
                            issue.getMessage()
                    )
            );
        }
        long issueSaveTook = System.currentTimeMillis() - issueSaveStart;

        long totalTook = System.currentTimeMillis() - totalStart;

        if (totalTook > 100) {
            log.info("[PERF] snapshot.rebuild.cached joinKey={} orderRows={} feeRows={} issues={} aggregate={}ms buildIssues={}ms snapshotSave={}ms issueDelete={}ms issueSave={}ms total={}ms",
                    joinKey,
                    orderRows.size(),
                    feeRows.size(),
                    issues.size(),
                    aggregateAmountTook,
                    issueBuildTook,
                    snapshotSaveTook,
                    issueDeleteTook,
                    issueSaveTook,
                    totalTook
            );
        }
    }

    private Map<String, List<SettlementOrderRow>> buildOrderRowsByJoinKey(List<SettlementOrderRow> rows) {
        Map<String, List<SettlementOrderRow>> result = new HashMap<>();
        for (SettlementOrderRow row : rows) {
            String joinKey = SettlementJoinKeyResolver.resolve(
                    row.getProductOrderNo(),
                    row.getOrderNo()
            );
            if (isBlank(joinKey)) {
                continue;
            }
            result.computeIfAbsent(joinKey, key -> new ArrayList<>()).add(row);
        }
        return result;
    }

    private Map<String, List<SettlementFeeRow>> buildFeeRowsByJoinKey(List<SettlementFeeRow> rows) {
        Map<String, List<SettlementFeeRow>> result = new HashMap<>();
        for (SettlementFeeRow row : rows) {
            String joinKey = SettlementJoinKeyResolver.resolve(
                    row.getProductOrderNo(),
                    row.getOrderNo()
            );
            if (isBlank(joinKey)) {
                continue;
            }
            result.computeIfAbsent(joinKey, key -> new ArrayList<>()).add(row);
        }
        return result;
    }

    private Map<String, SettlementOrderSnapshot> buildSnapshotByJoinKey(List<SettlementOrderSnapshot> snapshots) {
        return snapshots.stream()
                .collect(Collectors.toMap(
                        SettlementOrderSnapshot::getJoinKey,
                        Function.identity(),
                        (a, b) -> a
                ));
    }

    private List<SettlementIssue> buildIssues(
            SettlementOrderRow representativeOrderRow,
            String joinKey,
            String orderNo,
            String productOrderNo,
            MatchStatus matchStatus,
            boolean settlementMatched,
            boolean commissionMatched,
            boolean netMatched,
            boolean explainableNetDifference,
            BigDecimal orderSettlementAmount,
            BigDecimal feeSettlementAmount,
            BigDecimal orderCommissionAmount,
            BigDecimal feeCommissionAmount,
            BigDecimal orderNetAmount,
            BigDecimal feeNetAmount
    ) {
        List<SettlementIssue> issues = new ArrayList<>();

        if (matchStatus == MatchStatus.ORDER_ONLY) {
            issues.add(SettlementIssue.create(
                    null,
                    SettlementIssueType.ORDER_ROW_UNMATCHED,
                    orderNo,
                    productOrderNo,
                    joinKey,
                    "건별 정산 row는 존재하지만 수수료 상세 row가 아직 연결되지 않았습니다."
                            + buildSectionSuffix(representativeOrderRow)
            ));
            return issues;
        }

        if (matchStatus == MatchStatus.FEE_ONLY) {
            issues.add(SettlementIssue.create(
                    null,
                    SettlementIssueType.FEE_ROW_UNMATCHED,
                    orderNo,
                    productOrderNo,
                    joinKey,
                    "수수료 상세 row는 존재하지만 건별 정산 row가 아직 연결되지 않았습니다."
            ));
            return issues;
        }

        if (!settlementMatched) {
            issues.add(SettlementIssue.create(
                    null,
                    SettlementIssueType.SETTLEMENT_AMOUNT_MISMATCH,
                    orderNo,
                    productOrderNo,
                    joinKey,
                    "정산금액 불일치: order=" + printable(orderSettlementAmount) + ", fee=" + printable(feeSettlementAmount)
                            + buildSectionSuffix(representativeOrderRow)
            ));
        }

        if (!commissionMatched) {
            issues.add(SettlementIssue.create(
                    null,
                    SettlementIssueType.COMMISSION_AMOUNT_MISMATCH,
                    orderNo,
                    productOrderNo,
                    joinKey,
                    "수수료 금액 불일치: order=" + printable(orderCommissionAmount) + ", fee=" + printable(feeCommissionAmount)
                            + buildSectionSuffix(representativeOrderRow)
            ));
        }

        if (!netMatched && !explainableNetDifference) {
            issues.add(SettlementIssue.create(
                    null,
                    SettlementIssueType.NET_AMOUNT_MISMATCH,
                    orderNo,
                    productOrderNo,
                    joinKey,
                    "실수령액 불일치: order=" + printable(orderNetAmount) + ", fee=" + printable(feeNetAmount)
                            + buildSectionSuffix(representativeOrderRow)
            ));
        }

        return issues;
    }

    private List<SettlementOrderRow> findOrderRowsByJoinKey(String joinKey) {
        if (isBlank(joinKey)) {
            return List.of();
        }

        if (joinKey.startsWith("P:")) {
            return orderRowRepository.findAllByProductOrderNo(joinKey.substring(2));
        }

        if (joinKey.startsWith("O:")) {
            return orderRowRepository.findAllByOrderNo(joinKey.substring(2));
        }

        return List.of();
    }

    private List<SettlementFeeRow> findFeeRowsByJoinKey(String joinKey) {
        if (isBlank(joinKey)) {
            return List.of();
        }

        if (joinKey.startsWith("P:")) {
            return feeRowRepository.findAllByProductOrderNo(joinKey.substring(2));
        }

        if (joinKey.startsWith("O:")) {
            return feeRowRepository.findAllByOrderNo(joinKey.substring(2));
        }

        return List.of();
    }

    private Optional<SettlementOrderRow> chooseRepresentativeOrderRow(List<SettlementOrderRow> rows) {
        return rows.stream()
                .filter(Objects::nonNull)
                .max((a, b) -> Long.compare(nullSafeLong(a.getId()), nullSafeLong(b.getId())));
    }

    private Optional<SettlementFeeRow> chooseRepresentativeFeeRow(List<SettlementFeeRow> rows) {
        return rows.stream()
                .filter(Objects::nonNull)
                .max((a, b) -> Long.compare(nullSafeLong(a.getId()), nullSafeLong(b.getId())));
    }

    private BigDecimal aggregateOrderSettlementAmount(List<SettlementOrderRow> rows) {
        if (rows.isEmpty()) {
            return null;
        }

        return rows.stream()
                .map(SettlementOrderRow::getSettlementBaseAmount)
                .map(this::nullSafe)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal aggregateOrderCommissionAmount(List<SettlementOrderRow> rows) {
        if (rows.isEmpty()) {
            return null;
        }

        return rows.stream()
                .map(this::resolveOrderCommissionAmount)
                .map(this::nullSafe)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal aggregateOrderNetAmount(List<SettlementOrderRow> rows) {
        if (rows.isEmpty()) {
            return null;
        }

        return rows.stream()
                .map(SettlementOrderRow::getSettlementExpectedAmount)
                .map(this::nullSafe)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal aggregateOrderBenefitAmount(List<SettlementOrderRow> rows) {
        if (rows.isEmpty()) {
            return null;
        }

        return rows.stream()
                .map(SettlementOrderRow::getBenefitAmount)
                .map(this::nullSafe)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal aggregateFeeSettlementAmount(List<SettlementFeeRow> rows) {
        if (rows.isEmpty()) {
            return null;
        }

        Set<BigDecimal> distinctBaseAmounts = new LinkedHashSet<>();
        for (SettlementFeeRow row : rows) {
            if (row.getFeeBaseAmount() != null) {
                distinctBaseAmounts.add(row.getFeeBaseAmount());
            }
        }

        if (distinctBaseAmounts.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return distinctBaseAmounts.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal aggregateFeeCommissionAmount(List<SettlementFeeRow> rows) {
        if (rows.isEmpty()) {
            return null;
        }

        return rows.stream()
                .map(SettlementFeeRow::getCommissionAmount)
                .map(this::nullSafe)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal aggregateFeeNetAmount(
            BigDecimal feeSettlementAmount,
            BigDecimal feeCommissionAmount,
            List<SettlementFeeRow> rows
    ) {
        if (rows.isEmpty()) {
            return null;
        }

        BigDecimal settlement = feeSettlementAmount != null ? feeSettlementAmount : BigDecimal.ZERO;
        BigDecimal commission = feeCommissionAmount != null ? feeCommissionAmount : BigDecimal.ZERO;
        return settlement.subtract(commission);
    }

    private LocalDate aggregatePaidAt(List<SettlementOrderRow> rows) {
        return rows.stream()
                .map(SettlementOrderRow::getPaymentDate)
                .filter(Objects::nonNull)
                .min(LocalDate::compareTo)
                .orElse(null);
    }

    private LocalDate aggregateOrderSettlementDate(List<SettlementOrderRow> rows) {
        return rows.stream()
                .map(this::resolveOrderSettlementDate)
                .filter(Objects::nonNull)
                .max(LocalDate::compareTo)
                .orElse(null);
    }

    private LocalDate aggregateFeeSettlementDate(List<SettlementFeeRow> rows) {
        return rows.stream()
                .map(this::resolveFeeSettlementDate)
                .filter(Objects::nonNull)
                .max(LocalDate::compareTo)
                .orElse(null);
    }

    private MatchStatus resolveMatchStatus(
            List<SettlementOrderRow> orderRows,
            List<SettlementFeeRow> feeRows,
            boolean settlementMatched,
            boolean commissionMatched,
            boolean netMatched,
            boolean explainableNetDifference
    ) {
        if (!orderRows.isEmpty() && !feeRows.isEmpty()) {
            return (settlementMatched && commissionMatched && (netMatched || explainableNetDifference))
                    ? MatchStatus.MATCHED
                    : MatchStatus.MISMATCHED;
        }

        if (!orderRows.isEmpty()) {
            return MatchStatus.ORDER_ONLY;
        }

        return MatchStatus.FEE_ONLY;
    }

    private void createStandaloneIssue(
            SettlementIssueType issueType,
            String orderNo,
            String productOrderNo,
            String joinKey,
            String message
    ) {
        issueRepository.save(SettlementIssue.create(
                null,
                issueType,
                orderNo,
                productOrderNo,
                joinKey,
                message
        ));
    }

    private BigDecimal resolveOrderCommissionAmount(SettlementOrderRow orderRow) {
        BigDecimal result = BigDecimal.ZERO;

        if (orderRow.getNpayFeeAmount() != null) {
            result = result.add(orderRow.getNpayFeeAmount());
        }

        if (!isDeliverySection(orderRow) && orderRow.getSalesLinkedFeeTotal() != null) {
            result = result.add(orderRow.getSalesLinkedFeeTotal());
        }

        if (orderRow.getInstallmentFeeAmount() != null) {
            result = result.add(orderRow.getInstallmentFeeAmount());
        }

        return result;
    }

    private LocalDate resolveOrderSettlementDate(SettlementOrderRow orderRow) {
        if (orderRow.getSettlementCompletedDate() != null) {
            return orderRow.getSettlementCompletedDate();
        }
        return orderRow.getSettlementScheduledDate();
    }

    private LocalDate resolveFeeSettlementDate(SettlementFeeRow feeRow) {
        if (feeRow.getSettlementCompletedDate() != null) {
            return feeRow.getSettlementCompletedDate();
        }
        return feeRow.getSettlementScheduledDate();
    }

    private boolean isExplainableNetDifference(
            SettlementOrderRow representativeOrderRow,
            BigDecimal orderNetAmount,
            BigDecimal feeNetAmount,
            BigDecimal orderBenefitAmount
    ) {
        if (orderNetAmount == null || feeNetAmount == null) {
            return false;
        }

        if (isDeliverySection(representativeOrderRow)) {
            return true;
        }

        BigDecimal benefitAmount = orderBenefitAmount != null ? orderBenefitAmount : BigDecimal.ZERO;
        return benefitAmount.compareTo(BigDecimal.ZERO) > 0
                && orderNetAmount.add(benefitAmount).compareTo(feeNetAmount) == 0;
    }

    private boolean isDeliverySection(SettlementOrderRow row) {
        if (row == null || isBlank(row.getSectionType())) {
            return false;
        }
        return "배송비".equals(row.getSectionType().trim());
    }

    private String buildSectionSuffix(SettlementOrderRow row) {
        if (row == null || isBlank(row.getSectionType())) {
            return "";
        }
        return " (sectionType=" + row.getSectionType().trim() + ")";
    }

    private BigDecimal preferMatchedValue(BigDecimal a, BigDecimal b) {
        if (a == null && b == null) {
            return null;
        }
        if (a != null) {
            return a;
        }
        return b;
    }

    private boolean compareNullable(BigDecimal a, BigDecimal b) {
        if (a == null && b == null) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        return a.compareTo(b) == 0;
    }

    private BigDecimal nullSafe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private Long nullSafeLong(Long value) {
        return value == null ? 0L : value;
    }

    private String firstNonBlank(String a, String b) {
        if (!isBlank(a)) {
            return a;
        }
        return isBlank(b) ? null : b;
    }

    private <T> T firstNonNull(T a, T b) {
        return a != null ? a : b;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String printable(BigDecimal value) {
        return value == null ? "null" : value.toPlainString();
    }
}