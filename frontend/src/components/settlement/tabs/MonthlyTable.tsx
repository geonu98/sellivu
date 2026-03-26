import type { MonthlyRow } from "../../../types/settlementAnalysis";
import { formatNumber } from "../../../utils/number";

type Props = {
  rows: MonthlyRow[];
};

export default function MonthlyTable({ rows }: Props) {
  if (rows.length === 0) {
    return (
      <div className="rounded-xl border bg-white p-6 text-sm text-slate-500">
        월별 데이터가 없습니다.
      </div>
    );
  }

  return (
    <div className="overflow-x-auto rounded-xl border bg-white">
      <table className="min-w-full text-sm">
        <thead className="bg-slate-50 text-left">
          <tr>
            <th className="px-4 py-3">월</th>
            <th className="px-4 py-3">정산금액</th>
            <th className="px-4 py-3">일반정산</th>
            <th className="px-4 py-3">빠른정산</th>
            <th className="px-4 py-3">정산기준금액</th>
            <th className="px-4 py-3">총 수수료</th>
            <th className="px-4 py-3">혜택정산</th>
            <th className="px-4 py-3">일별 공제/환급</th>
            <th className="px-4 py-3">보류금액</th>
            <th className="px-4 py-3">비즈월렛 상계</th>
            <th className="px-4 py-3">반품안심케어</th>
            <th className="px-4 py-3">우대수수료 환급</th>
            <th className="px-4 py-3">집계 건수</th>
          </tr>
        </thead>
        <tbody>
          {rows.map((row) => (
            <tr key={row.yearMonth} className="border-t">
              <td className="px-4 py-3">{row.yearMonth}</td>
              <td className="px-4 py-3">{formatNumber(row.settlementAmount)}</td>
              <td className="px-4 py-3">{formatNumber(row.generalSettlementAmount)}</td>
              <td className="px-4 py-3">{formatNumber(row.fastSettlementAmount)}</td>
              <td className="px-4 py-3">{formatNumber(row.settlementBaseAmount)}</td>
              <td className="px-4 py-3">{formatNumber(row.totalFeeAmount)}</td>
              <td className="px-4 py-3">{formatNumber(row.benefitSettlementAmount)}</td>
              <td className="px-4 py-3">{formatNumber(row.dailyDeductionRefundAmount)}</td>
              <td className="px-4 py-3">{formatNumber(row.holdAmount)}</td>
              <td className="px-4 py-3">{formatNumber(row.bizWalletOffsetAmount)}</td>
              <td className="px-4 py-3">{formatNumber(row.safeReturnCareCost)}</td>
              <td className="px-4 py-3">{formatNumber(row.preferredFeeRefundAmount)}</td>
              <td className="px-4 py-3">{formatNumber(row.rowCount)}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}