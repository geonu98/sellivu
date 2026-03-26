import type { SnapshotRow } from "../../../types/settlementAnalysis";
import {
  matchStatusBadgeClass,
  reviewStatusBadgeClass,
} from "../../../utils/settlementBadge";
import { formatDate } from "../../../utils/date";
import { formatNumber } from "../../../utils/number";

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

export default function SnapshotsTable({
  rows,
  page,
  totalElements,
  totalPages,
  hasNext,
  loading = false,
  onPageChange,
}: Props) {
  if (rows.length === 0 && !loading) {
    return (
      <div className="rounded-xl border bg-white p-6 text-sm text-slate-500">
        스냅샷 데이터가 없습니다.
      </div>
    );
  }

  return (
    <div className="space-y-4">
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
            {rows.length === 0 ? (
              <tr>
                <td
                  colSpan={10}
                  className="px-4 py-10 text-center text-sm text-slate-500"
                >
                  데이터를 불러오는 중이거나 표시할 스냅샷이 없습니다.
                </td>
              </tr>
            ) : (
              rows.map((row) => (
                <tr key={row.id} className="border-t">
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
                  <td className="px-4 py-3">
                    {formatNumber(row.resolvedNetAmount)}
                  </td>
                  <td className="px-4 py-3">{row.issueCount}</td>
                  <td className="px-4 py-3">
                    <span className={reviewStatusBadgeClass(row.reviewStatus)}>
                      {row.reviewStatus}
                    </span>
                  </td>
                </tr>
              ))
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