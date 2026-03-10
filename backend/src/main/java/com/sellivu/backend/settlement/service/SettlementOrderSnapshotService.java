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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SettlementOrderSnapshotService {

    private final SettlementOrderRowRepository orderRowRepository;
    private final SettlementFeeRowRepository feeRowRepository;
    private final SettlementOrderSnapshotRepository snapshotRepository;
    private final SettlementIssueRepository issueRepository;

    @Transactional
    public void aggregateForUpload(Long uploadId, SettlementFileType fileType) {
        if (fileType == SettlementFileType.ORDER_SETTLEMENT) {
            List<SettlementOrderRow> orderRows = orderRowRepository.findAllByUploadId(uploadId);
            for (SettlementOrderRow orderRow : orderRows) {
                aggregateByOrderRow(orderRow);
            }
            return;
        }

        if (fileType == SettlementFileType.FEE_DETAIL) {
            List<SettlementFeeRow> feeRows = feeRowRepository.findAllByUploadId(uploadId);
            for (SettlementFeeRow feeRow : feeRows) {
                aggregateByFeeRow(feeRow);
            }
        }
    }

    @Transactional
    public void aggregateByOrderRow(SettlementOrderRow orderRow) {
        String joinKey = resolveJoinKey(orderRow.getProductOrderNo(), orderRow.getOrderNo());
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
        String joinKey = resolveJoinKey(feeRow.getProductOrderNo(), feeRow.getOrderNo());
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
        List<SettlementOrderRow> orderRows = findOrderRowsByJoinKey(joinKey);
        List<SettlementFeeRow> feeRows = findFeeRowsByJoinKey(joinKey);

        if (orderRows.isEmpty() && feeRows.isEmpty()) {
            return;
        }

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

        SettlementOrderSnapshot snapshot = snapshotRepository.findByJoinKey(joinKey)
                .orElseGet(() -> SettlementOrderSnapshot.create(
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
                ));

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

        SettlementOrderSnapshot saved = snapshotRepository.save(snapshot);

        issueRepository.deleteAllBySnapshotId(saved.getId());
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

    /**
     * fee 상세는 한 주문에 대해 여러 행이 생기며,
     * feeBaseAmount는 수수료 항목별로 반복 기재되는 경우가 많다.
     * 그래서 단순 sum이 아니라 "중복 제거 후 합산"으로 처리한다.
     */
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

    private String resolveJoinKey(String productOrderNo, String orderNo) {
        if (!isBlank(productOrderNo)) {
            return "P:" + productOrderNo.trim();
        }
        if (!isBlank(orderNo)) {
            return "O:" + orderNo.trim();
        }
        return null;
    }

    private BigDecimal resolveOrderCommissionAmount(SettlementOrderRow orderRow) {
        BigDecimal result = BigDecimal.ZERO;

        if (orderRow.getNpayFeeAmount() != null) {
            result = result.add(orderRow.getNpayFeeAmount());
        }

        // 배송비는 판매수수료(매출연동/판매수수료) 미적용
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

        // 배송비는 판매수수료 미적용 + 정산 흐름 차이로 오탐이 잘 날 수 있어 보수적으로 처리
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

    private boolean isProductSection(SettlementOrderRow row) {
        if (row == null || isBlank(row.getSectionType())) {
            return false;
        }
        return "상품주문".equals(row.getSectionType().trim());
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