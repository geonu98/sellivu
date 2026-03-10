import type { DailyRow } from "../../../types/settlementAnalysis";
import { formatDate } from "../../../utils/date";
import { formatNumber } from "../../../utils/number";

type Props = {
  rows: DailyRow[];
};

export default function DailyTable({ rows }: Props) {
  if (rows.length === 0) {
    return (
      <div className="rounded-xl border bg-white p-6 text-sm text-slate-500">
        일별 데이터가 없습니다.
      </div>
    );
  }

  return (
    <div className="overflow-x-auto rounded-xl border bg-white">
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
              <td className="px-4 py-3">{formatDate(row.settlementDate)}</td>
              <td className="px-4 py-3">{formatNumber(row.settlementAmount)}</td>
              <td className="px-4 py-3">{formatNumber(row.benefitAmount)}</td>
              <td className="px-4 py-3">{formatNumber(row.deductionRefundAmount)}</td>
              <td className="px-4 py-3">{formatNumber(row.bizWalletOffsetAmount)}</td>
              <td className="px-4 py-3">{formatNumber(row.safeReturnCareAmount)}</td>
              <td className="px-4 py-3">{formatNumber(row.preferredFeeRefundAmount)}</td>
              <td className="px-4 py-3">{row.settlementMethod || "-"}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}