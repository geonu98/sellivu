import type { DailyRow } from "../../../types/settlementAnalysis";
import { formatDate } from "../../../utils/date";
import { formatNumber } from "../../../utils/number";

type Props = {
  rows: DailyRow[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  hasNext: boolean;
  loading?: boolean;
  onPageChange: (nextPage: number) => void;
};

export default function DailyTable({
  rows,
  page,
  size,
  totalElements,
  totalPages,
  hasNext,
  loading = false,
  onPageChange,
}: Props) {
  if (!loading && totalElements === 0) {
    return (
      <div className="rounded-xl border bg-white p-6 text-sm text-slate-500">
        일별 데이터가 없습니다.
      </div>
    );
  }

  const start = totalElements === 0 ? 0 : page * size + 1;
  const end = totalElements === 0 ? 0 : Math.min((page + 1) * size, totalElements);

  return (
    <div className="rounded-xl border bg-white">
      <div className="overflow-x-auto">
        <table className="min-w-full text-sm">
          <thead className="bg-slate-50 text-left">
            <tr>
              <th className="px-4 py-3">정산완료일</th>
              <th className="px-4 py-3">정산금액</th>
              <th className="px-4 py-3">혜택정산</th>
              <th className="px-4 py-3">일별 공제/환급</th>
              <th className="px-4 py-3">비즈월렛 상계</th>
              <th className="px-4 py-3">반품안심케어</th>
              <th className="px-4 py-3">우대수수료 환급</th>
              <th className="px-4 py-3">정산방식</th>
            </tr>
          </thead>
          <tbody>
            {rows.map((row) => (
              <tr key={row.id} className="border-t">
                <td className="px-4 py-3">
                  {formatDate(row.settlementCompletedDate)}
                </td>
                <td className="px-4 py-3">
                  {formatNumber(row.settlementAmount)}
                </td>
                <td className="px-4 py-3">
                  {formatNumber(row.benefitSettlementAmount)}
                </td>
                <td className="px-4 py-3">
                  {formatNumber(row.dailyDeductionRefundAmount)}
                </td>
                <td className="px-4 py-3">
                  {formatNumber(row.bizWalletOffsetAmount)}
                </td>
                <td className="px-4 py-3">
                  {formatNumber(row.safeReturnCareCost)}
                </td>
                <td className="px-4 py-3">
                  {formatNumber(row.preferredFeeRefundAmount)}
                </td>
                <td className="px-4 py-3">{row.settlementMethod || "-"}</td>
              </tr>
            ))}

            {loading && rows.length === 0 && (
              <tr>
                <td colSpan={8} className="px-4 py-8 text-center text-slate-500">
                  불러오는 중...
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      <div className="flex items-center justify-between border-t px-4 py-3 text-sm">
        <div className="text-slate-500">
          {totalElements > 0
            ? `${start}-${end} / 전체 ${totalElements}건`
            : "데이터 0건"}
        </div>

        <div className="flex items-center gap-2">
          <button
            type="button"
            onClick={() => onPageChange(page - 1)}
            disabled={loading || page <= 0}
            className="rounded-lg border px-3 py-1.5 font-medium text-slate-700 disabled:cursor-not-allowed disabled:opacity-40"
          >
            이전
          </button>

          <span className="text-slate-600">
            {totalPages > 0 ? `${page + 1} / ${totalPages}` : "0 / 0"}
          </span>

          <button
            type="button"
            onClick={() => onPageChange(page + 1)}
            disabled={loading || !hasNext}
            className="rounded-lg border px-3 py-1.5 font-medium text-slate-700 disabled:cursor-not-allowed disabled:opacity-40"
          >
            다음
          </button>
        </div>
      </div>
    </div>
  );
}