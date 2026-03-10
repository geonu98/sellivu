import type { SnapshotRow } from "../../../types/settlementAnalysis";
import {
  matchStatusBadgeClass,
  reviewStatusBadgeClass,
} from "../../../utils/settlementBadge";
import { formatDate } from "../../../utils/date";
import { formatNumber } from "../../../utils/number";

type Props = {
  rows: SnapshotRow[];
};

export default function SnapshotsTable({ rows }: Props) {
  if (rows.length === 0) {
    return (
      <div className="rounded-xl border bg-white p-6 text-sm text-slate-500">
        스냅샷 데이터가 없습니다.
      </div>
    );
  }

  return (
    <div className="overflow-x-auto rounded-xl border bg-white">
      <table className="min-w-full text-sm">
        <thead className="bg-slate-50 text-left">
          <tr>
            <th className="px-4 py-3">상품주문번호</th>
            <th className="px-4 py-3">주문번호</th>
            <th className="px-4 py-3">상품명</th>
            <th className="px-4 py-3">정산일</th>
            <th className="px-4 py-3">매칭상태</th>
            <th className="px-4 py-3">실정산금액</th>
            <th className="px-4 py-3">수수료</th>
            <th className="px-4 py-3">순정산금액</th>
            <th className="px-4 py-3">이슈 수</th>
            <th className="px-4 py-3">리뷰 상태</th>
          </tr>
        </thead>
        <tbody>
          {rows.map((row) => (
            <tr key={row.snapshotId} className="border-t">
              <td className="px-4 py-3">{row.productOrderNo || "-"}</td>
              <td className="px-4 py-3">{row.orderNo || "-"}</td>
              <td className="px-4 py-3">{row.productName || "-"}</td>
              <td className="px-4 py-3">{formatDate(row.settlementDate)}</td>
              <td className="px-4 py-3">
                <span className={matchStatusBadgeClass(row.matchStatus)}>
                  {row.matchStatus}
                </span>
              </td>
              <td className="px-4 py-3">
                {formatNumber(row.resolvedSettlementAmount)}
              </td>
              <td className="px-4 py-3">
                {formatNumber(row.resolvedCommissionAmount)}
              </td>
              <td className="px-4 py-3">{formatNumber(row.resolvedNetAmount)}</td>
              <td className="px-4 py-3">{row.issueCount}</td>
              <td className="px-4 py-3">
                <span className={reviewStatusBadgeClass(row.reviewStatus)}>
                  {row.reviewStatus}
                </span>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}