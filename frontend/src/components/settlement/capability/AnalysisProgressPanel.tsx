type Props = {
  visible: boolean;
  step: number;
};

const steps = [
  "파일 연결 상태 확인 중...",
  "스냅샷 재구성 준비 중...",
  "정산 규칙 대조 중...",
  "이슈 필터링 및 결과 정리 중...",
];

export default function AnalysisProgressPanel({ visible, step }: Props) {
  if (!visible) return null;

  return (
    <div className="rounded-xl border bg-white p-4">
      <h3 className="mb-3 text-sm font-semibold">분석 진행 상황</h3>

      <div className="space-y-3">
        {steps.map((label, index) => {
          const done = step > index;
          const active = step === index;

          return (
            <div key={label} className="flex items-center gap-3">
              <div
                className={`flex h-6 w-6 items-center justify-center rounded-full text-xs font-semibold ${
                  done
                    ? "bg-green-100 text-green-700"
                    : active
                      ? "bg-blue-100 text-blue-700"
                      : "bg-slate-100 text-slate-500"
                }`}
              >
                {done ? "✓" : index + 1}
              </div>

              <div
                className={`text-sm ${
                  done
                    ? "text-green-700"
                    : active
                      ? "font-medium text-blue-700"
                      : "text-slate-500"
                }`}
              >
                {label}
              </div>
            </div>
          );
        })}
      </div>

      <div className="mt-4 h-2 overflow-hidden rounded-full bg-slate-100">
        <div
          className="h-full rounded-full bg-blue-500 transition-all duration-300"
          style={{ width: `${((step + 1) / steps.length) * 100}%` }}
        />
      </div>
    </div>
  );
}