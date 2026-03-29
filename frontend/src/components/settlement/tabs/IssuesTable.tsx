import { useMemo, useState } from "react";
import type { IssueRow, SnapshotRow } from "../../../types/settlementAnalysis";
import { severityBadgeClass } from "../../../utils/settlementBadge";
import { formatDate } from "../../../utils/date";

type IssueFilter = "ACTION_REQUIRED" | "ALL" | "EXPLAINABLE" | "REFUND";

type Props = {
  rows: SnapshotRow[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  hasNext: boolean;
  loading?: boolean;
  onPageChange: (page: number) => void;
};

const ISSUE_ORDER_ONLY = 1 << 0;
const ISSUE_FEE_ONLY = 1 << 1;
const ISSUE_SETTLEMENT_MISMATCH = 1 << 2;
const ISSUE_COMMISSION_MISMATCH = 1 << 3;
const ISSUE_NET_MISMATCH = 1 << 4;


function sourceTypeLabel(sourceType: string | null | undefined) {
  switch (sourceType) {
    case "SNAPSHOT":
      return "비교 기준 데이터";
    default:
      return sourceType || "-";
  }
}

function severityLabel(severity: string | null | undefined) {
  switch (severity) {
    case "ERROR":
      return "오류";
    case "WARN":
      return "주의";
    case "INFO":
      return "안내";
    default:
      return severity || "-";
  }
}

function safeSeverityClass(severity: string | null | undefined) {
  return severity
    ? severityBadgeClass(severity)
    : "rounded-full bg-slate-100 px-2 py-1 text-xs text-slate-500";
}

function buildCheckValues(row: IssueRow) {
  const values: string[] = [];

  if (row.orderNo) values.push(`주문번호: ${row.orderNo}`);
  if (row.productOrderNo) values.push(`상품주문번호: ${row.productOrderNo}`);
  if (typeof row.issueDate === "string" && row.issueDate) {
    values.push(`정산일: ${formatDate(row.issueDate)}`);
  }

  return values;
}

function isRefundCandidate(row: IssueRow) {
  return row.refundCandidate === true;
}

function isExplainable(row: IssueRow) {
  return row.judgementStatus === "EXPLAINABLE";
}

function isActionRequired(row: IssueRow) {
  return row.judgementStatus === "PENDING" || row.needsUserInput === true;
}

function renderStatusBadges(row: IssueRow) {
  const badges = [];

  badges.push(
    <span
      key="status"
      className={`inline-flex rounded-full px-2.5 py-1 text-[11px] font-semibold ${
        row.judgementStatus === "EXPLAINABLE"
          ? "bg-blue-50 text-blue-700"
          : "bg-rose-50 text-rose-700"
      }`}
    >
      {row.statusLabel || "확인 필요"}
    </span>
  );

  if (row.refundCandidate) {
    badges.push(
      <span
        key="refund"
        className="inline-flex rounded-full bg-emerald-50 px-2.5 py-1 text-[11px] font-semibold text-emerald-700"
      >
        환급 후보
      </span>
    );
  }

  if (row.needsUserInput) {
    badges.push(
      <span
        key="input"
        className="inline-flex rounded-full bg-purple-50 px-2.5 py-1 text-[11px] font-semibold text-purple-700"
      >
        옵션 확인 필요
      </span>
    );
  }

  return badges;
}
function calcDiff(
  a: number | null | undefined,
  b: number | null | undefined
) {
  if (a == null || b == null) return null;
  return a - b;
}

function formatDiffText(diff: number | null | undefined) {
  if (diff == null) return null;
  return Number.isInteger(diff) ? String(diff) : diff.toFixed(2);
}



function buildIssueRowsFromSnapshot(snapshot: SnapshotRow): IssueRow[] {
  if (!snapshot.hasIssue || snapshot.issueCount <= 0) {
    return [];
  }

  const settlementDiff = calcDiff(
    snapshot.orderSettlementAmount,
    snapshot.feeSettlementAmount
  );
  const commissionDiff = calcDiff(
    snapshot.orderCommissionAmount,
    snapshot.feeCommissionAmount
  );
  const netDiff = calcDiff(snapshot.orderNetAmount, snapshot.feeNetAmount);

  const issueRows: IssueRow[] = [];

 const pushIssue = (
  issueType: string,
  defaults: {
    title: string;
    description: string;
    impact: string;
    actionGuide: string;
    message: string;
    refundCandidate?: boolean;
    needsUserInput?: boolean;
    explainable?: boolean;
    diff?: number | null | undefined;
  }
) => {
    const diffText = formatDiffText(defaults.diff);
    const description =
      diffText != null
        ? `${defaults.description} (차이: ${diffText})`
        : defaults.description;

    issueRows.push({
      id: null,
      sourceType: "SNAPSHOT",
      snapshotId: snapshot.id,
      issueType,
      orderNo: snapshot.orderNo,
      productOrderNo: snapshot.productOrderNo,
      joinKey: snapshot.joinKey,
      message: defaults.message,
      resolved: false,
      severity: "ERROR",
      judgementStatus: defaults.explainable ? "EXPLAINABLE" : "PENDING",
      explanationCode: snapshot.primaryIssueCode,
      needsUserInput:
        defaults.needsUserInput ?? snapshot.needsUserInput ?? false,
      possibleReasonMessage: null,
      issueDate: snapshot.settlementDate,
      createdAt: snapshot.lastAggregatedAt,

      displayCategory: "정산 비교 결과",
      title: defaults.title,
      description,
      impact: defaults.impact,
      actionGuide: defaults.actionGuide,
      statusLabel: defaults.explainable ? "설명 가능" : "확인 필요",
      explainable: defaults.explainable ?? false,
      refundCandidate:
        defaults.refundCandidate ?? snapshot.refundCandidate ?? false,
    });
  };

  if ((snapshot.issueMask & ISSUE_ORDER_ONLY) !== 0) {
    pushIssue("ORDER_ONLY", {
      title: "주문 정산 데이터만 존재",
      description: "주문 정산 데이터는 있지만 수수료 데이터가 없습니다.",
      impact: "비교 기준이 한쪽만 있어 수수료 검증이 불완전합니다.",
      actionGuide: "수수료 상세 파일 업로드 여부와 조인 기준을 확인하세요.",
      message: "주문 정산 데이터만 있고 수수료 데이터가 없습니다.",
      needsUserInput: true,
    });
  }

  if ((snapshot.issueMask & ISSUE_FEE_ONLY) !== 0) {
    pushIssue("FEE_ONLY", {
      title: "수수료 데이터만 존재",
      description: "수수료 데이터는 있지만 주문 정산 데이터가 없습니다.",
      impact: "주문 기준 금액 검증이 불완전합니다.",
      actionGuide: "주문 정산 파일 업로드 여부와 조인 기준을 확인하세요.",
      message: "수수료 데이터만 있고 주문 정산 데이터가 없습니다.",
      needsUserInput: true,
    });
  }

  if ((snapshot.issueMask & ISSUE_SETTLEMENT_MISMATCH) !== 0) {
    pushIssue("SETTLEMENT_MISMATCH", {
      title: "정산 금액 불일치",
      description: "주문 정산 금액과 수수료 기준 금액이 서로 다릅니다.",
      impact: "실제 정산 기준 금액 해석이 어긋날 수 있습니다.",
      actionGuide: "정산 기준 금액과 주문/수수료 원본 값을 비교하세요.",
      message: "정산 금액이 일치하지 않습니다.",
      explainable: true,
      diff: settlementDiff,
    });
  }

  if ((snapshot.issueMask & ISSUE_COMMISSION_MISMATCH) !== 0) {
    pushIssue("COMMISSION_MISMATCH", {
      title: "수수료 금액 불일치",
      description: "주문 정산 수수료와 수수료 상세 합계가 다릅니다.",
      impact: "실수령액과 마진 계산에 직접 영향을 줍니다.",
      actionGuide:
        "수수료 항목별 합계와 우대 수수료/환급 가능성을 확인하세요.",
      message: "수수료 금액이 일치하지 않습니다.",
      refundCandidate: snapshot.refundCandidate,
      explainable: true,
      diff: commissionDiff,
    });
  }

  if ((snapshot.issueMask & ISSUE_NET_MISMATCH) !== 0) {
    pushIssue("NET_MISMATCH", {
      title: "실수령 금액 불일치",
      description: "최종 실수령 금액이 기준 데이터와 다릅니다.",
      impact: "판매자 정산 해석과 순이익 계산이 달라질 수 있습니다.",
      actionGuide: "정산 금액, 수수료, 공제 항목을 함께 재확인하세요.",
      message: "실수령 금액이 일치하지 않습니다.",
      explainable: true,
      diff: netDiff,
    });
  }

  if (issueRows.length === 0) {
    pushIssue(snapshot.primaryIssueCode || "ISSUE", {
      title: "이슈 감지",
      description: "정산 비교 과정에서 확인이 필요한 항목이 감지되었습니다.",
      impact: "정산 해석 결과를 다시 확인할 필요가 있습니다.",
      actionGuide: "대표 이슈 코드와 금액 비교 결과를 확인하세요.",
      message: "확인이 필요한 이슈가 있습니다.",
      refundCandidate: snapshot.refundCandidate,
      needsUserInput: snapshot.needsUserInput,
      explainable: snapshot.reviewStatus !== "PENDING",
    });
  }

  return issueRows;
}

export default function IssuesTable({
  rows,
  page,
  totalElements,
  totalPages,
  hasNext,
  loading = false,
  onPageChange,
}: Props) {
  const [filter, setFilter] = useState<IssueFilter>("ACTION_REQUIRED");

  const issueRows = useMemo(
    () => rows.flatMap((snapshot) => buildIssueRowsFromSnapshot(snapshot)),
    [rows]
  );

  const summary = useMemo(() => {
    const total = issueRows.length;
    const refundCount = issueRows.filter(isRefundCandidate).length;
    const explainableCount = issueRows.filter(
      (row) => isExplainable(row) && !isRefundCandidate(row)
    ).length;
    const actionRequiredCount = issueRows.filter(
      (row) => isActionRequired(row) && !isRefundCandidate(row)
    ).length;

    return {
      total,
      refundCount,
      explainableCount,
      actionRequiredCount,
    };
  }, [issueRows]);

  const filteredRows = useMemo(() => {
    switch (filter) {
      case "ACTION_REQUIRED":
        return issueRows.filter(
          (row) => isActionRequired(row) && !isRefundCandidate(row)
        );
      case "EXPLAINABLE":
        return issueRows.filter(
          (row) => isExplainable(row) && !isRefundCandidate(row)
        );
      case "REFUND":
        return issueRows.filter(isRefundCandidate);
      case "ALL":
      default:
        return issueRows;
    }
  }, [issueRows, filter]);

  if (issueRows.length === 0 && !loading) {
    return (
      <div className="rounded-xl border bg-white p-6 text-sm text-slate-500">
        이슈 데이터가 없습니다.
      </div>
    );
  }

  const filterButtons: { key: IssueFilter; label: string; count: number }[] = [
    {
      key: "ACTION_REQUIRED",
      label: "확인 필요",
      count: summary.actionRequiredCount,
    },
    {
      key: "EXPLAINABLE",
      label: "설명 가능",
      count: summary.explainableCount,
    },
    { key: "REFUND", label: "환급 후보", count: summary.refundCount },
    { key: "ALL", label: "현재 페이지 전체", count: summary.total },
  ];

  return (
    <div className="space-y-4">
      <div className="rounded-xl border bg-white p-4">
        <div className="flex flex-col gap-3 lg:flex-row lg:items-start lg:justify-between">
          <div className="space-y-2">
            <div className="text-xs font-semibold uppercase tracking-[0.12em] text-slate-400">
              옵션 기준 분류 결과
            </div>

            <p className="text-sm leading-6 text-slate-700">
              전체 이슈는{" "}
              <span className="font-semibold text-slate-900">
                {totalElements.toLocaleString()}건
              </span>
              이고, 현재 페이지에서는{" "}
              <span className="font-semibold text-rose-600">
                확인 필요 {summary.actionRequiredCount}건
              </span>
              ,{" "}
              <span className="font-semibold text-blue-600">
                설명 가능 {summary.explainableCount}건
              </span>
              ,{" "}
              <span className="font-semibold text-emerald-600">
                환급 후보 {summary.refundCount}건
              </span>
              입니다.
            </p>

            <p className="text-xs text-slate-500">
              상단 분류 수치는 현재 페이지 기준입니다.
            </p>
          </div>

          <div className="flex flex-wrap gap-2">
            <span className="rounded-full bg-rose-50 px-3 py-1 text-xs font-semibold text-rose-700">
              확인 필요 {summary.actionRequiredCount}
            </span>
            <span className="rounded-full bg-blue-50 px-3 py-1 text-xs font-semibold text-blue-700">
              설명 가능 {summary.explainableCount}
            </span>
            <span className="rounded-full bg-emerald-50 px-3 py-1 text-xs font-semibold text-emerald-700">
              환급 후보 {summary.refundCount}
            </span>
          </div>
        </div>
      </div>

      <div className="flex flex-wrap gap-2">
        {filterButtons.map((button) => {
          const active = filter === button.key;

          return (
            <button
              key={button.key}
              type="button"
              onClick={() => setFilter(button.key)}
              className={`rounded-full border px-3 py-2 text-xs font-semibold transition ${
                active
                  ? "border-slate-900 bg-slate-900 text-white"
                  : "border-slate-200 bg-white text-slate-600 hover:border-slate-300"
              }`}
            >
              {button.label} {button.count}
            </button>
          );
        })}
      </div>

      <div className="overflow-x-auto rounded-xl border bg-white">
        <table className="min-w-full text-sm">
          <thead className="bg-slate-50 text-left text-xs font-semibold text-slate-500">
            <tr>
              <th className="px-4 py-3">문제</th>
              <th className="px-4 py-3">영향</th>
              <th className="px-4 py-3">확인할 값</th>
              <th className="px-4 py-3">상태</th>
              <th className="px-4 py-3">권장 조치</th>
              <th className="px-4 py-3">발생일</th>
            </tr>
          </thead>

          <tbody>
            {filteredRows.length === 0 ? (
              <tr>
                <td
                  colSpan={6}
                  className="px-4 py-10 text-center text-sm text-slate-500"
                >
                  현재 페이지에서 선택한 필터에 해당하는 이슈가 없습니다.
                </td>
              </tr>
            ) : (
              filteredRows.map((row, index) => {
                const checkValues = buildCheckValues(row);

                return (
                  <tr
                    key={`${row.snapshotId}-${row.issueType}-${index}`}
                    className="border-t align-top"
                  >
                    <td className="px-4 py-4">
                      <div className="space-y-1">
                        <div className="text-xs font-semibold text-slate-500">
                          {row.displayCategory || "-"}
                        </div>

                        <div className="text-sm font-semibold leading-5 text-slate-900">
                          {row.title || row.message || "-"}
                        </div>

                        <div className="text-xs leading-5 text-slate-500">
                          {row.description || "-"}
                        </div>

                        <div className="pt-1 text-[11px] text-slate-400">
                          {sourceTypeLabel(row.sourceType)}
                        </div>
                      </div>
                    </td>

                    <td className="px-4 py-4">
                      <div className="space-y-2">
                        <p className="text-xs leading-5 text-amber-700">
                          {row.impact || "-"}
                        </p>

                        <div>
                          <span className={safeSeverityClass(row.severity)}>
                            {severityLabel(row.severity)}
                          </span>
                        </div>
                      </div>
                    </td>

                    <td className="px-4 py-4">
                      {checkValues.length > 0 ? (
                        <div className="space-y-1 text-xs text-slate-600">
                          {checkValues.map((value) => (
                            <div key={value}>{value}</div>
                          ))}
                        </div>
                      ) : (
                        <span className="text-xs text-slate-400">-</span>
                      )}
                    </td>

                    <td className="px-4 py-4">
                      <div className="flex flex-wrap gap-2">
                        {renderStatusBadges(row)}
                      </div>
                    </td>

                    <td className="px-4 py-4">
                      <p className="text-xs leading-5 text-slate-600">
                        {row.actionGuide || "-"}
                      </p>
                    </td>

                    <td className="px-4 py-4 text-xs text-slate-500">
                      {typeof row.issueDate === "string" && row.issueDate
                        ? formatDate(row.issueDate)
                        : formatDate(row.createdAt)}
                    </td>
                  </tr>
                );
              })
            )}
          </tbody>
        </table>
      </div>

      <div className="flex items-center justify-between rounded-xl border bg-white px-4 py-3">
        <p className="text-xs text-slate-500">
          총 {totalElements.toLocaleString()}건 · {page + 1} /{" "}
          {Math.max(totalPages, 1)} 페이지
        </p>

        <div className="flex gap-2">
          <button
            type="button"
            disabled={loading || page === 0}
            onClick={() => onPageChange(page - 1)}
            className="rounded-lg border border-slate-200 px-3 py-1.5 text-xs font-semibold text-slate-700 disabled:cursor-not-allowed disabled:opacity-40"
          >
            이전
          </button>
          <button
            type="button"
            disabled={loading || !hasNext}
            onClick={() => onPageChange(page + 1)}
            className="rounded-lg border border-slate-200 px-3 py-1.5 text-xs font-semibold text-slate-700 disabled:cursor-not-allowed disabled:opacity-40"
          >
            다음
          </button>
        </div>
      </div>
    </div>
  );
}