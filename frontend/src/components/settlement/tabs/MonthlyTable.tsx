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
            <th className="px-4 py-3">주문건수</th>
          </tr>
        </thead>
        <tbody>
          {rows.map((row) => (
            <tr key={row.month} className="border-t">
              <td className="px-4 py-3">{row.month}</td>
              <td className="px-4 py-3">{formatNumber(row.settlementAmount)}</td>
              <td className="px-4 py-3">{formatNumber(row.orderCount)}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}