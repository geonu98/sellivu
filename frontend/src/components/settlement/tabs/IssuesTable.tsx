import { useMemo, useState } from "react";
import type { IssueRow } from "../../../types/settlementAnalysis";
import { severityBadgeClass } from "../../../utils/settlementBadge";
import { formatDate } from "../../../utils/date";

type Props = {
  rows: IssueRow[];
};

type IssueFilter = "ACTION_REQUIRED" | "ALL" | "EXPLAINABLE" | "REFUND";

function sourceTypeLabel(sourceType: string | null | undefined) {
  switch (sourceType) {
    case "SNAPSHOT":
      return "비교 기준 데이터";
    case "DAILY_CROSS_CHECK":
      return "일별 정산 비교";
    case "MONTHLY_CROSS_CHECK":
      return "월별 정산 비교";
    case "ORDER_SETTLEMENT":
      return "건별 정산";
    case "FEE_DETAIL":
      return "수수료 상세";
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

  if (row.orderNo) {
    values.push(`주문번호: ${row.orderNo}`);
  }

  if (row.productOrderNo) {
    values.push(`상품주문번호: ${row.productOrderNo}`);
  }

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

export default function IssuesTable({ rows }: Props) {
  const [filter, setFilter] = useState<IssueFilter>("ACTION_REQUIRED");

  const summary = useMemo(() => {
    const total = rows.length;
    const refundCount = rows.filter(isRefundCandidate).length;
    const explainableCount = rows.filter(
      (row) => isExplainable(row) && !isRefundCandidate(row)
    ).length;
    const actionRequiredCount = rows.filter(
      (row) => isActionRequired(row) && !isRefundCandidate(row)
    ).length;

    return {
      total,
      refundCount,
      explainableCount,
      actionRequiredCount,
    };
  }, [rows]);

  const filteredRows = useMemo(() => {
    switch (filter) {
      case "ACTION_REQUIRED":
        return rows.filter(
          (row) => isActionRequired(row) && !isRefundCandidate(row)
        );
      case "EXPLAINABLE":
        return rows.filter(
          (row) => isExplainable(row) && !isRefundCandidate(row)
        );
      case "REFUND":
        return rows.filter(isRefundCandidate);
      case "ALL":
      default:
        return rows;
    }
  }, [rows, filter]);

  if (rows.length === 0) {
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
    {
      key: "REFUND",
      label: "환급 후보",
      count: summary.refundCount,
    },
    {
      key: "ALL",
      label: "전체",
      count: summary.total,
    },
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
              총 <span className="font-semibold text-slate-900">{summary.total}건</span>의
              이슈 중{" "}
              <span className="font-semibold text-rose-600">
                실제 확인이 필요한 건 {summary.actionRequiredCount}건
              </span>
              ,{" "}
              <span className="font-semibold text-blue-600">
                설명 가능한 차이 {summary.explainableCount}건
              </span>
              ,{" "}
              <span className="font-semibold text-emerald-600">
                환급 후보 {summary.refundCount}건
              </span>
              입니다.
            </p>

            <p className="text-xs text-slate-500">
              설정한 옵션을 기준으로 정책성 차이와 실제 확인이 필요한 이슈를 구분했습니다.
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
                  현재 필터에 해당하는 이슈가 없습니다.
                </td>
              </tr>
            ) : (
              filteredRows.map((row) => {
                const checkValues = buildCheckValues(row);

                return (
                  <tr
                    key={row.id ?? `${row.issueType}-${row.createdAt}`}
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
    </div>
  );
}