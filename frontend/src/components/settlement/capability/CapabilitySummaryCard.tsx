import type { AnalysisCapabilityResponse } from "../../../types/settlementAnalysis";
import { FILE_TYPE_LABEL } from "../../../utils/settlementLabels";

type Props = {
  capability: AnalysisCapabilityResponse | null;
};

export default function CapabilitySummaryCard({ capability }: Props) {
  if (!capability) {
    return (
      <div className="rounded-xl border bg-white p-4 text-sm text-slate-500">
        capability 정보가 없습니다.
      </div>
    );
  }

  return (
    <div className="rounded-xl border bg-white p-4">
      <h3 className="mb-3 text-base font-semibold">Capability 요약</h3>

      <div className="grid grid-cols-2 gap-4 text-sm">
        <div>
          <div className="mb-1 font-medium">연결 파일</div>
          <div className="text-slate-600">
            {capability.uploadedFileTypes.length > 0
              ? capability.uploadedFileTypes.map((v) => FILE_TYPE_LABEL[v]).join(", ")
              : "없음"}
          </div>
        </div>

        <div>
          <div className="mb-1 font-medium">누락 파일</div>
          <div className="text-slate-600">
            {capability.missingFileTypes.length > 0
              ? capability.missingFileTypes.map((v) => FILE_TYPE_LABEL[v]).join(", ")
              : "없음"}
          </div>
        </div>

        <div>
          <div className="mb-1 font-medium">사용 가능 뷰</div>
          <div className="text-slate-600">
            {capability.availableViews.length > 0
              ? capability.availableViews.join(", ")
              : "없음"}
          </div>
        </div>

        <div>
          <div className="mb-1 font-medium">옵션 입력 필요</div>
          <div className="text-slate-600">
            {capability.requiresContextOptions ? "예" : "아니오"}
          </div>
        </div>
      </div>

      {capability.gaps.length > 0 && (
        <div className="mt-4">
          <div className="mb-1 text-sm font-medium">분석 공백</div>
          <ul className="list-disc pl-5 text-sm text-slate-600">
            {capability.gaps.map((gap) => (
              <li key={gap}>{gap}</li>
            ))}
          </ul>
        </div>
      )}

      {capability.explainablePolicyFactors.length > 0 && (
        <div className="mt-4">
          <div className="mb-1 text-sm font-medium">설명 가능한 정책 요인</div>
          <div className="flex flex-wrap gap-2">
            {capability.explainablePolicyFactors.map((factor) => (
              <span
                key={factor}
                className="rounded-full bg-blue-50 px-2 py-1 text-xs text-blue-700"
              >
                {factor}
              </span>
            ))}
          </div>
        </div>
      )}

      {capability.verificationPendingFields.length > 0 && (
        <div className="mt-4">
          <div className="mb-1 text-sm font-medium">검증 대기 필드</div>
          <div className="flex flex-wrap gap-2">
            {capability.verificationPendingFields.map((field) => (
              <span
                key={field}
                className="rounded-full bg-yellow-50 px-2 py-1 text-xs text-yellow-700"
              >
                {field}
              </span>
            ))}
          </div>
        </div>
      )}

      <div className="mt-4 rounded-lg bg-slate-50 p-3 text-sm text-slate-700">
        {capability.message}
      </div>
    </div>
  );
}