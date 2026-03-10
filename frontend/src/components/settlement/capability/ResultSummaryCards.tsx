import type { IssueRow, SnapshotRow } from "../../../types/settlementAnalysis";

type Props = {
  issues: IssueRow[];
  snapshots: SnapshotRow[];
};

export default function ResultSummaryCards({ issues, snapshots }: Props) {
  const confirmedCount = issues.filter(
    (issue) => issue.judgementStatus === "CONFIRMED"
  ).length;
  const explainableCount = issues.filter(
    (issue) => issue.judgementStatus === "EXPLAINABLE"
  ).length;
  const pendingCount = issues.filter(
    (issue) => issue.judgementStatus === "PENDING"
  ).length;
  const matchedCount = snapshots.filter(
    (snapshot) => snapshot.matchStatus === "MATCHED"
  ).length;

  const cards = [
    {
      label: "전체 이슈",
      value: issues.length,
      className: "border-slate-200 bg-white text-slate-900",
      subClassName: "text-slate-500",
    },
    {
      label: "확정 이슈",
      value: confirmedCount,
      className: "border-red-200 bg-red-50 text-red-700",
      subClassName: "text-red-500",
    },
    {
      label: "설명 가능",
      value: explainableCount,
      className: "border-blue-200 bg-blue-50 text-blue-700",
      subClassName: "text-blue-500",
    },
    {
      label: "추가 확인 필요",
      value: pendingCount,
      className: "border-orange-200 bg-orange-50 text-orange-700",
      subClassName: "text-orange-500",
    },
    {
      label: "매칭 완료 스냅샷",
      value: matchedCount,
      className: "border-green-200 bg-green-50 text-green-700",
      subClassName: "text-green-500",
    },
  ];

  return (
    <div className="grid grid-cols-5 gap-3">
      {cards.map((card) => (
        <div
          key={card.label}
          className={`rounded-xl border p-4 ${card.className}`}
        >
          <div className={`text-xs ${card.subClassName}`}>{card.label}</div>
          <div className="mt-1 text-2xl font-semibold">{card.value}</div>
        </div>
      ))}
    </div>
  );
}