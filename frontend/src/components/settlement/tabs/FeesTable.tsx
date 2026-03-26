import type { FeeRow } from "../../../types/settlementAnalysis";
import { formatDate } from "../../../utils/date";
import { formatNumber } from "../../../utils/number";

type Props = {
  rows: FeeRow[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  hasNext: boolean;
  loading?: boolean;
  onPageChange: (page: number) => void;
};

export default function FeesTable({
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
        수수료 데이터가 없습니다.
      </div>
    );
  }

  return (
    <div className="space-y-4">
      <div className="overflow-x-auto rounded-xl border bg-white">
        <table className="min-w-full text-sm">
          <thead className="bg-slate-50 text-left">
            <tr>
              <th className="px-4 py-3">주문번호</th>
              <th className="px-4 py-3">상품주문번호</th>
              <th className="px-4 py-3">정산일</th>
              <th className="px-4 py-3">수수료금액</th>
              <th className="px-4 py-3">수수료유형</th>
              <th className="px-4 py-3">순금액</th>
            </tr>
          </thead>
          <tbody>
            {rows.length === 0 ? (
              <tr>
                <td
                  colSpan={6}
                  className="px-4 py-10 text-center text-sm text-slate-500"
                >
                  데이터를 불러오는 중이거나 표시할 수수료 데이터가 없습니다.
                </td>
              </tr>
            ) : (
              rows.map((row) => (
                <tr key={row.id} className="border-t">
                  <td className="px-4 py-3">{row.orderNo || "-"}</td>
                  <td className="px-4 py-3">{row.productOrderNo || "-"}</td>
                  <td className="px-4 py-3">{formatDate(row.settlementDate)}</td>
                  <td className="px-4 py-3">
                    {formatNumber(row.commissionAmount)}
                  </td>
                  <td className="px-4 py-3">{row.feeType || "-"}</td>
                  <td className="px-4 py-3">{formatNumber(row.netAmount)}</td>
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