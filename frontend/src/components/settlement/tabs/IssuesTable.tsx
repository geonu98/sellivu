import type { IssueRow } from "../../../types/settlementAnalysis";
import { severityBadgeClass } from "../../../utils/settlementBadge";
import { formatDate } from "../../../utils/date";

type Props = {
  rows: IssueRow[];
};

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

function renderStatusBadges(row: IssueRow) {
  const badges = [];

  badges.push(
    <span
      key="status"
      className={`inline-flex rounded-full px-2.5 py-1 text-[11px] font-semibold ${
        row.statusLabel === "설명 가능"
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
  if (rows.length === 0) {
    return (
      <div className="rounded-xl border bg-white p-6 text-sm text-slate-500">
        이슈 데이터가 없습니다.
      </div>
    );
  }

  return (
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
          {rows.map((row) => {
            const checkValues = buildCheckValues(row);

            return (
              <tr key={row.id ?? `${row.issueType}-${row.createdAt}`} className="border-t align-top">
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
          })}
        </tbody>
      </table>
    </div>
  );
}