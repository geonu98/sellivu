import type { OrderRow } from "../../../types/settlementAnalysis";
import { formatDate } from "../../../utils/date";
import { formatNumber } from "../../../utils/number";

type Props = {
  rows: OrderRow[];
};

export default function OrdersTable({ rows }: Props) {
  if (rows.length === 0) {
    return (
      <div className="rounded-xl border bg-white p-6 text-sm text-slate-500">
        건별 데이터가 없습니다.
      </div>
    );
  }

  return (
    <div className="overflow-x-auto rounded-xl border bg-white">
      <table className="min-w-full text-sm">
        <thead className="bg-slate-50 text-left">
          <tr>
            <th className="px-4 py-3">주문번호</th>
            <th className="px-4 py-3">상품주문번호</th>
            <th className="px-4 py-3">상품명</th>
            <th className="px-4 py-3">정산일</th>
            <th className="px-4 py-3">정산금액</th>
            <th className="px-4 py-3">혜택정산</th>
            <th className="px-4 py-3">순정산금액</th>
          </tr>
        </thead>
        <tbody>
          {rows.map((row) => (
            <tr key={row.id} className="border-t">
              <td className="px-4 py-3">{row.orderNo || "-"}</td>
              <td className="px-4 py-3">{row.productOrderNo || "-"}</td>
              <td className="px-4 py-3">{row.productName || "-"}</td>
              <td className="px-4 py-3">{formatDate(row.settlementDate)}</td>
              <td className="px-4 py-3">{formatNumber(row.settlementAmount)}</td>
              <td className="px-4 py-3">{formatNumber(row.benefitAmount)}</td>
              <td className="px-4 py-3">{formatNumber(row.netAmount)}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}