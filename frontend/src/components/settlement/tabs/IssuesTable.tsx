import type { IssueRow } from "../../../types/settlementAnalysis";
import {
  judgementBadgeClass,
  severityBadgeClass,
} from "../../../utils/settlementBadge";
import { formatDate } from "../../../utils/date";

type Props = {
  rows: IssueRow[];
};

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
        <thead className="bg-slate-50 text-left">
          <tr>
            <th className="px-4 py-3">구분</th>
            <th className="px-4 py-3">메시지</th>
            <th className="px-4 py-3">심각도</th>
            <th className="px-4 py-3">판정</th>
            <th className="px-4 py-3">설명 코드</th>
            <th className="px-4 py-3">옵션 확인</th>
            <th className="px-4 py-3">발생일</th>
          </tr>
        </thead>
        <tbody>
          {rows.map((row) => (
            <tr key={row.id} className="border-t align-top">
              <td className="px-4 py-3">
                <div className="font-medium">{row.issueType}</div>
                <div className="mt-1 text-xs text-slate-500">{row.sourceType}</div>
                <div className="mt-1 text-xs text-slate-500">
                  {row.productOrderNo || row.orderNo || row.joinKey || "-"}
                </div>
              </td>
              <td className="px-4 py-3">
                <div className="font-medium">{row.message}</div>
                {row.possibleReasonMessage && (
                  <div className="mt-1 text-xs text-slate-500">
                    {row.possibleReasonMessage}
                  </div>
                )}
              </td>
              <td className="px-4 py-3">
                <span className={severityBadgeClass(row.severity)}>
                  {row.severity}
                </span>
              </td>
              <td className="px-4 py-3">
                <span className={judgementBadgeClass(row.judgementStatus)}>
                  {row.judgementStatus}
                </span>
              </td>
              <td className="px-4 py-3 text-xs text-slate-600">
                {row.explanationCode || "-"}
              </td>
              <td className="px-4 py-3">
                {row.needsUserInput ? (
                  <span className="rounded-full bg-purple-100 px-2 py-1 text-xs text-purple-700">
                    옵션 확인 필요
                  </span>
                ) : (
                  "-"
                )}
              </td>
              <td className="px-4 py-3">{formatDate(row.issueDate)}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}