import type { AnalysisSetResponse } from "../../../types/settlementAnalysis";

type Props = {
  analysisSets: AnalysisSetResponse[];
  selectedAnalysisSetId: number | null;
  onSelect: (id: number) => void;
};

export default function AnalysisSetList({
  analysisSets,
  selectedAnalysisSetId,
  onSelect,
}: Props) {
  const safeAnalysisSets = Array.isArray(analysisSets) ? analysisSets : [];

  console.log("AnalysisSetList props.analysisSets =", analysisSets);

  return (
    <div className="rounded-xl border bg-white p-4">
      <h3 className="mb-3 text-sm font-semibold">분석 세트 목록</h3>

      <div className="space-y-2">
        {safeAnalysisSets.length === 0 && (
          <div className="text-sm text-slate-500">생성된 분석 세트가 없습니다.</div>
        )}

        {safeAnalysisSets.map((set) => {
          const selected = set.id === selectedAnalysisSetId;

          return (
            <button
              key={set.id}
              onClick={() => onSelect(set.id)}
              className={`w-full rounded-lg border px-3 py-2 text-left text-sm ${
                selected
                  ? "border-black bg-slate-50 font-semibold"
                  : "border-slate-200"
              }`}
            >
              <div>{set.name}</div>
              <div className="mt-1 text-xs text-slate-500">ID: {set.id}</div>
            </button>
          );
        })}
      </div>
    </div>
  );
}