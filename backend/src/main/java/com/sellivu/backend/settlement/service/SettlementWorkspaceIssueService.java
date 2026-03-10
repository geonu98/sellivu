package com.sellivu.backend.settlement.service;

import com.sellivu.backend.settlement.domain.*;
import com.sellivu.backend.settlement.dto.SettlementAnalysisIssueResponse;
import com.sellivu.backend.settlement.repository.SettlementDailyRowRepository;
import com.sellivu.backend.settlement.repository.SettlementOrderSnapshotRepository;
import com.sellivu.backend.settlement.repository.SettlementWorkspaceContextRepository;
import com.sellivu.backend.settlement.repository.SettlementWorkspaceFileRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SettlementWorkspaceIssueService {

    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final BigDecimal AMOUNT_TOLERANCE = new BigDecimal("0.01");

    private final SettlementWorkspaceService settlementWorkspaceService;
    private final SettlementWorkspaceFileRepository settlementWorkspaceFileRepository;
    private final SettlementWorkspaceContextRepository settlementWorkspaceContextRepository;
    private final SettlementOrderSnapshotRepository settlementOrderSnapshotRepository;
    private final SettlementDailyRowRepository settlementDailyRowRepository;

    @Transactional
    public List<SettlementAnalysisIssueResponse> getIssues(String workspaceKey, String workspaceToken) {
        SettlementWorkspace workspace =
                settlementWorkspaceService.validateAccessibleWorkspace(workspaceKey, workspaceToken);

        UploadIdGroup uploadIdGroup = extractUploadIds(workspace.getId());

        List<SettlementOrderSnapshot> snapshots = loadSnapshots(uploadIdGroup);
        List<SettlementDailyRow> dailyRows = loadDailyRows(uploadIdGroup);
        SettlementWorkspaceContext context = loadContext(workspace.getId());

        List<SettlementIssue> issues = new ArrayList<>();
        issues.addAll(buildSnapshotIssues(snapshots));
        issues.addAll(buildDailyCrossCheckIssues(workspace.getId(), snapshots, dailyRows, context));

        List<SettlementAnalysisIssueResponse> responses = new ArrayList<>();
        for (SettlementIssue issue : issues) {
            if (issue.getJoinKey() != null && issue.getJoinKey().startsWith(dailyJoinKeyPrefix(workspace.getId()))) {
                responses.add(SettlementAnalysisIssueResponse.fromDailyEntity(
                        issue,
                        extractIssueDate(issue.getJoinKey())
                ));
            } else {
                responses.add(SettlementAnalysisIssueResponse.fromEntity(issue));
            }
        }

        responses.sort((a, b) -> {
            Long aId = a.getId() == null ? 0L : a.getId();
            Long bId = b.getId() == null ? 0L : b.getId();
            return Long.compare(bId, aId);
        });

        return responses;
    }

    private SettlementWorkspaceContext loadContext(Long workspaceId) {
        return settlementWorkspaceContextRepository.findByWorkspaceId(workspaceId)
                .orElseGet(() -> SettlementWorkspaceContext.createDefault(workspaceId));
    }

    private List<SettlementOrderSnapshot> loadSnapshots(UploadIdGroup uploadIdGroup) {
        if (uploadIdGroup.orderUploadIds.isEmpty() && uploadIdGroup.feeUploadIds.isEmpty()) {
            return Collections.emptyList();
        }

        return settlementOrderSnapshotRepository
                .findAllByOrderUploadIdInOrFeeUploadIdInOrderByIdDesc(
                        uploadIdGroup.orderUploadIds,
                        uploadIdGroup.feeUploadIds
                );
    }

    private List<SettlementDailyRow> loadDailyRows(UploadIdGroup uploadIdGroup) {
        if (uploadIdGroup.dailyUploadIds.isEmpty()) {
            return Collections.emptyList();
        }

        return settlementDailyRowRepository
                .findAllByUploadIdInOrderBySettlementCompletedDateAscIdAsc(uploadIdGroup.dailyUploadIds);
    }

    private UploadIdGroup extractUploadIds(Long workspaceId) {
        List<SettlementWorkspaceFile> files =
                settlementWorkspaceFileRepository.findAllByWorkspaceIdAndActiveTrueOrderByIdAsc(workspaceId);

        List<Long> dailyUploadIds = new ArrayList<>();
        List<Long> orderUploadIds = new ArrayList<>();
        List<Long> feeUploadIds = new ArrayList<>();

        for (SettlementWorkspaceFile file : files) {
            if (file.getUploadId() == null || file.getFileType() == null) {
                continue;
            }

            switch (file.getFileType()) {
                case DAILY_SETTLEMENT -> dailyUploadIds.add(file.getUploadId());
                case ORDER_SETTLEMENT -> orderUploadIds.add(file.getUploadId());
                case FEE_DETAIL -> feeUploadIds.add(file.getUploadId());
            }
        }

        return new UploadIdGroup(dailyUploadIds, orderUploadIds, feeUploadIds);
    }

    private List<SettlementIssue> buildSnapshotIssues(List<SettlementOrderSnapshot> snapshots) {
        List<SettlementIssue> issues = new ArrayList<>();

        for (SettlementOrderSnapshot snapshot : snapshots) {
            if (snapshot.getMatchStatus() == null) {
                continue;
            }

            MatchStatus status = snapshot.getMatchStatus();

            if (status == MatchStatus.ORDER_ONLY) {
                issues.add(SettlementIssue.createDetailed(
                        snapshot.getId(),
                        SettlementIssueType.ORDER_ROW_UNMATCHED,
                        snapshot.getOrderNo(),
                        snapshot.getProductOrderNo(),
                        snapshot.getJoinKey(),
                        buildOrderOnlyMessage(snapshot),
                        IssueSeverity.ERROR,
                        IssueJudgementStatus.CONFIRMED,
                        null,
                        false
                ));
                continue;
            }

            if (status == MatchStatus.FEE_ONLY) {
                issues.add(SettlementIssue.createDetailed(
                        snapshot.getId(),
                        SettlementIssueType.FEE_ROW_UNMATCHED,
                        snapshot.getOrderNo(),
                        snapshot.getProductOrderNo(),
                        snapshot.getJoinKey(),
                        buildFeeOnlyMessage(snapshot),
                        IssueSeverity.ERROR,
                        IssueJudgementStatus.CONFIRMED,
                        null,
                        false
                ));
                continue;
            }

            if (status == MatchStatus.MISMATCHED) {
                appendMismatchIssues(issues, snapshot);
            }
        }

        return issues;
    }

    private void appendMismatchIssues(
            List<SettlementIssue> issues,
            SettlementOrderSnapshot snapshot
    ) {
        if (!isSame(snapshot.getOrderSettlementAmount(), snapshot.getFeeSettlementAmount())) {
            issues.add(SettlementIssue.createDetailed(
                    snapshot.getId(),
                    SettlementIssueType.SETTLEMENT_AMOUNT_MISMATCH,
                    snapshot.getOrderNo(),
                    snapshot.getProductOrderNo(),
                    snapshot.getJoinKey(),
                    buildAmountMismatchMessage(
                            "주문 정산금액",
                            snapshot.getOrderSettlementAmount(),
                            "수수료 정산금액",
                            snapshot.getFeeSettlementAmount()
                    ),
                    IssueSeverity.ERROR,
                    IssueJudgementStatus.CONFIRMED,
                    null,
                    false
            ));
        }

        if (!isSame(snapshot.getOrderCommissionAmount(), snapshot.getFeeCommissionAmount())) {
            issues.add(SettlementIssue.createDetailed(
                    snapshot.getId(),
                    SettlementIssueType.COMMISSION_AMOUNT_MISMATCH,
                    snapshot.getOrderNo(),
                    snapshot.getProductOrderNo(),
                    snapshot.getJoinKey(),
                    buildAmountMismatchMessage(
                            "주문 수수료",
                            snapshot.getOrderCommissionAmount(),
                            "수수료 상세 수수료",
                            snapshot.getFeeCommissionAmount()
                    ),
                    IssueSeverity.ERROR,
                    IssueJudgementStatus.CONFIRMED,
                    null,
                    false
            ));
        }

        if (!snapshot.isNetAmountMatched()
                && !isSame(snapshot.getOrderNetAmount(), snapshot.getFeeNetAmount())) {

            issues.add(SettlementIssue.createDetailed(
                    snapshot.getId(),
                    SettlementIssueType.NET_AMOUNT_MISMATCH,
                    snapshot.getOrderNo(),
                    snapshot.getProductOrderNo(),
                    snapshot.getJoinKey(),
                    buildAmountMismatchMessage(
                            "주문 실정산금액",
                            snapshot.getOrderNetAmount(),
                            "수수료 상세 실정산금액",
                            snapshot.getFeeNetAmount()
                    ),
                    IssueSeverity.ERROR,
                    IssueJudgementStatus.CONFIRMED,
                    null,
                    false
            ));
        }
    }

    private List<SettlementIssue> buildDailyCrossCheckIssues(
            Long workspaceId,
            List<SettlementOrderSnapshot> snapshots,
            List<SettlementDailyRow> dailyRows,
            SettlementWorkspaceContext context
    ) {
        List<SettlementIssue> issues = new ArrayList<>();

        Map<LocalDate, BigDecimal> snapshotSumByDate = snapshots.stream()
                .filter(snapshot -> snapshot.getSettlementDate() != null)
                .collect(Collectors.groupingBy(
                        SettlementOrderSnapshot::getSettlementDate,
                        TreeMap::new,
                        Collectors.reducing(
                                ZERO,
                                snapshot -> nvl(snapshot.getOrderNetAmount()),
                                BigDecimal::add
                        )
                ));

        Map<LocalDate, List<SettlementDailyRow>> dailyRowsByDate = dailyRows.stream()
                .filter(row -> row.getSettlementCompletedDate() != null)
                .collect(Collectors.groupingBy(
                        SettlementDailyRow::getSettlementCompletedDate,
                        TreeMap::new,
                        Collectors.toList()
                ));

        Set<LocalDate> allDates = new TreeSet<>();
        allDates.addAll(snapshotSumByDate.keySet());
        allDates.addAll(dailyRowsByDate.keySet());

        for (LocalDate settlementDate : allDates) {
            BigDecimal snapshotSum = nvl(snapshotSumByDate.get(settlementDate));
            List<SettlementDailyRow> rows = dailyRowsByDate.getOrDefault(settlementDate, Collections.emptyList());

            if (rows.isEmpty()) {
                if (snapshotSum.compareTo(ZERO) > 0) {
                    issues.add(SettlementIssue.createDetailed(
                            null,
                            SettlementIssueType.DAILY_ROW_MISSING,
                            null,
                            null,
                            dailyJoinKey(workspaceId, settlementDate),
                            String.format(
                                    "해당 정산일(%s)의 snapshot 합계는 %s원이지만 일별 정산 행이 없습니다.",
                                    settlementDate,
                                    snapshotSum
                            ),
                            IssueSeverity.ERROR,
                            IssueJudgementStatus.CONFIRMED,
                            null,
                            false
                    ));
                }
                continue;
            }

            if (rows.size() > 1) {
                issues.add(SettlementIssue.createDetailed(
                        null,
                        SettlementIssueType.DAILY_ROW_DUPLICATED,
                        null,
                        null,
                        dailyJoinKey(workspaceId, settlementDate),
                        String.format(
                                "해당 정산일(%s)의 일별 정산 행이 %d건 존재합니다.",
                                settlementDate,
                                rows.size()
                        ),
                        IssueSeverity.ERROR,
                        IssueJudgementStatus.CONFIRMED,
                        null,
                        false
                ));
                continue;
            }

            SettlementDailyRow dailyRow = rows.get(0);
            BigDecimal dailyAmount = extractDailyCompareAmount(dailyRow);

            if (!isSame(snapshotSum, dailyAmount)) {
                DailyIssueMeta meta = classifyDailyAmountMismatch(dailyRow, context);

                issues.add(SettlementIssue.createDetailed(
                        null,
                        SettlementIssueType.DAILY_AMOUNT_MISMATCH,
                        null,
                        null,
                        dailyJoinKey(workspaceId, settlementDate),
                        String.format(
                                "해당 정산일(%s)의 snapshot 합계(%s원)와 일별 정산 금액(%s원)이 일치하지 않습니다.",
                                settlementDate,
                                snapshotSum,
                                dailyAmount
                        ),
                        meta.severity(),
                        meta.judgementStatus(),
                        meta.explanationCode(),
                        meta.needsUserInput()
                ));
            }
        }

        return issues;
    }

    private DailyIssueMeta classifyDailyAmountMismatch(
            SettlementDailyRow dailyRow,
            SettlementWorkspaceContext context
    ) {
        if (hasNonZero(dailyRow.getBenefitSettlementAmount())) {
            if (isAnyYes(
                    context.getStoreCouponUsage(),
                    context.getNaverCouponUsage(),
                    context.getPointBenefitUsage()
            )) {
                return explainable(IssueExplanationCode.BENEFIT_SETTLEMENT_POSSIBLE);
            }

            if (isAllNo(
                    context.getStoreCouponUsage(),
                    context.getNaverCouponUsage(),
                    context.getPointBenefitUsage()
            )) {
                return pending(IssueExplanationCode.BENEFIT_SETTLEMENT_POSSIBLE);
            }

            return explainable(IssueExplanationCode.BENEFIT_SETTLEMENT_POSSIBLE);
        }

        if (hasNonZero(dailyRow.getDailyDeductionRefundAmount())) {
            if (context.getClaimIncluded() == AnalysisOptionValue.YES) {
                return explainable(IssueExplanationCode.DAILY_POLICY_ADJUSTMENT_POSSIBLE);
            }
            if (context.getClaimIncluded() == AnalysisOptionValue.NO) {
                return pending(IssueExplanationCode.DAILY_POLICY_ADJUSTMENT_POSSIBLE);
            }
            return explainable(IssueExplanationCode.DAILY_POLICY_ADJUSTMENT_POSSIBLE);
        }

        if (hasNonZero(dailyRow.getBizWalletOffsetAmount())) {
            if (context.getBizWalletOffsetUsage() == AnalysisOptionValue.YES) {
                return explainable(IssueExplanationCode.BIZ_WALLET_OFFSET_POSSIBLE);
            }
            if (context.getBizWalletOffsetUsage() == AnalysisOptionValue.NO) {
                return pending(IssueExplanationCode.BIZ_WALLET_OFFSET_POSSIBLE);
            }
            return explainable(IssueExplanationCode.BIZ_WALLET_OFFSET_POSSIBLE);
        }

        if (hasNonZero(dailyRow.getSafeReturnCareCost())) {
            if (context.getSafeReturnCareUsage() == AnalysisOptionValue.YES) {
                return explainable(IssueExplanationCode.SAFE_RETURN_CARE_POSSIBLE);
            }
            if (context.getSafeReturnCareUsage() == AnalysisOptionValue.NO) {
                return pending(IssueExplanationCode.SAFE_RETURN_CARE_POSSIBLE);
            }
            return explainable(IssueExplanationCode.SAFE_RETURN_CARE_POSSIBLE);
        }

        if (hasNonZero(dailyRow.getFastSettlementAmount())) {
            if (context.getFastSettlementUsage() == AnalysisOptionValue.YES) {
                return explainable(IssueExplanationCode.FAST_SETTLEMENT_POSSIBLE);
            }
            if (context.getFastSettlementUsage() == AnalysisOptionValue.NO) {
                return pending(IssueExplanationCode.FAST_SETTLEMENT_POSSIBLE);
            }
            return explainable(IssueExplanationCode.FAST_SETTLEMENT_POSSIBLE);
        }

        if (hasNonZero(dailyRow.getPreferredFeeRefundAmount())) {
            return explainable(IssueExplanationCode.PREFERRED_FEE_REFUND_POSSIBLE);
        }

        if (dailyRow.getSettlementMethod() != null && !dailyRow.getSettlementMethod().isBlank()) {
            return new DailyIssueMeta(
                    IssueSeverity.WARN,
                    IssueJudgementStatus.PENDING,
                    IssueExplanationCode.SETTLEMENT_METHOD_REVIEW_REQUIRED,
                    true
            );
        }

        return pending(IssueExplanationCode.CONTEXT_OPTION_REVIEW_REQUIRED);
    }

    private DailyIssueMeta explainable(IssueExplanationCode code) {
        return new DailyIssueMeta(
                IssueSeverity.WARN,
                IssueJudgementStatus.EXPLAINABLE,
                code,
                false
        );
    }

    private DailyIssueMeta pending(IssueExplanationCode code) {
        return new DailyIssueMeta(
                IssueSeverity.WARN,
                IssueJudgementStatus.PENDING,
                code,
                true
        );
    }

    private boolean isAnyYes(AnalysisOptionValue... values) {
        for (AnalysisOptionValue value : values) {
            if (value == AnalysisOptionValue.YES) {
                return true;
            }
        }
        return false;
    }

    private boolean isAllNo(AnalysisOptionValue... values) {
        for (AnalysisOptionValue value : values) {
            if (value != AnalysisOptionValue.NO) {
                return false;
            }
        }
        return true;
    }

    private BigDecimal extractDailyCompareAmount(SettlementDailyRow dailyRow) {
        return dailyRow.getSettlementAmount() != null ? dailyRow.getSettlementAmount() : ZERO;
    }

    private String dailyJoinKeyPrefix(Long workspaceId) {
        return "WS:" + workspaceId + ":DATE:";
    }

    private String dailyJoinKey(Long workspaceId, LocalDate settlementDate) {
        return dailyJoinKeyPrefix(workspaceId) + settlementDate;
    }

    private LocalDate extractIssueDate(String joinKey) {
        if (joinKey == null || !joinKey.contains(":DATE:")) {
            return null;
        }
        try {
            String dateText = joinKey.substring(joinKey.lastIndexOf(":DATE:") + 6);
            return LocalDate.parse(dateText);
        } catch (Exception e) {
            return null;
        }
    }

    private String buildOrderOnlyMessage(SettlementOrderSnapshot snapshot) {
        return String.format(
                "주문 데이터만 존재하고 수수료 상세 데이터가 없습니다. joinKey=%s, orderNo=%s, productOrderNo=%s",
                blankIfNull(snapshot.getJoinKey()),
                blankIfNull(snapshot.getOrderNo()),
                blankIfNull(snapshot.getProductOrderNo())
        );
    }

    private String buildFeeOnlyMessage(SettlementOrderSnapshot snapshot) {
        return String.format(
                "수수료 상세 데이터만 존재하고 주문 데이터가 없습니다. joinKey=%s, orderNo=%s, productOrderNo=%s",
                blankIfNull(snapshot.getJoinKey()),
                blankIfNull(snapshot.getOrderNo()),
                blankIfNull(snapshot.getProductOrderNo())
        );
    }

    private String buildAmountMismatchMessage(
            String leftLabel,
            BigDecimal leftValue,
            String rightLabel,
            BigDecimal rightValue
    ) {
        return String.format(
                "%s(%s원)과 %s(%s원)이 일치하지 않습니다.",
                leftLabel,
                nvl(leftValue),
                rightLabel,
                nvl(rightValue)
        );
    }

    private boolean isSame(BigDecimal a, BigDecimal b) {
        return nvl(a).subtract(nvl(b)).abs().compareTo(AMOUNT_TOLERANCE) <= 0;
    }

    private boolean hasNonZero(BigDecimal value) {
        return value != null && value.compareTo(ZERO) != 0;
    }

    private BigDecimal nvl(BigDecimal value) {
        return value == null ? ZERO : value;
    }

    private String blankIfNull(String value) {
        return value == null ? "" : value;
    }

    private record UploadIdGroup(
            List<Long> dailyUploadIds,
            List<Long> orderUploadIds,
            List<Long> feeUploadIds
    ) {
    }

    private record DailyIssueMeta(
            IssueSeverity severity,
            IssueJudgementStatus judgementStatus,
            IssueExplanationCode explanationCode,
            boolean needsUserInput
    ) {
    }
}