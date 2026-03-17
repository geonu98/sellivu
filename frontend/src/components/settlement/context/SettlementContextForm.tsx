import type {
  AnalysisContextResponse,
  UpdateAnalysisContextRequest,
  AnalysisOptionValue,
} from "../../../types/settlementAnalysis";

type Props = {
  value: AnalysisContextResponse | null;
  highlight?: boolean;
  onChange: (next: UpdateAnalysisContextRequest) => void;
};

const OPTION_KEYS = [
  "storeCouponUsage",
  "naverCouponUsage",
  "pointBenefitUsage",
  "safeReturnCareUsage",
  "bizWalletOffsetUsage",
  "fastSettlementUsage",
  "claimIncluded",
] as const;

type OptionKey = (typeof OPTION_KEYS)[number];

const OPTION_CHOICES: AnalysisOptionValue[] = ["YES", "NO", "UNKNOWN"];

const optionConfigs: {
  key: OptionKey;
  label: string;
  description: string;
}[] = [
  {
    key: "storeCouponUsage",
    label: "스토어 할인 쿠폰 사용 여부",
    description: "스토어 자체 할인 쿠폰으로 인한 정산 차이 여부를 확인합니다.",
  },
  {
    key: "naverCouponUsage",
    label: "네이버 발행 쿠폰 사용 여부",
    description: "네이버 발행 쿠폰 반영으로 인한 정책성 차이를 판정 보조합니다.",
  },
  {
    key: "pointBenefitUsage",
    label: "적립 혜택 사용 여부",
    description:
      "구매적립, 리뷰적립, 고객관리 적립 등 혜택성 차이를 묶어서 확인합니다.",
  },
  {
    key: "safeReturnCareUsage",
    label: "반품안심케어 사용 여부",
    description: "반품안심케어 비용으로 인한 explainable 차이 여부를 확인합니다.",
  },
  {
    key: "bizWalletOffsetUsage",
    label: "마이너스 비즈월렛 상계 여부",
    description:
      "주문 단위가 아니라 계정/지갑 단위 상계로 발생한 차이 가능성을 확인합니다.",
  },
  {
    key: "fastSettlementUsage",
    label: "빠른정산 사용 여부",
    description: "빠른정산 정책 반영 여부를 판정 보조 입력으로 받습니다.",
  },
  {
    key: "claimIncluded",
    label: "취소/반품/교환 포함 여부",
    description:
      "취소, 반품, 교환 등 사후 조정 항목 포함으로 발생한 차이 가능성을 확인합니다.",
  },
];

function normalizeContext(
  value: AnalysisContextResponse | null
): UpdateAnalysisContextRequest {
  return {
    storeCouponUsage: value?.storeCouponUsage ?? "UNKNOWN",
    naverCouponUsage: value?.naverCouponUsage ?? "UNKNOWN",
    pointBenefitUsage: value?.pointBenefitUsage ?? "UNKNOWN",
    safeReturnCareUsage: value?.safeReturnCareUsage ?? "UNKNOWN",
    bizWalletOffsetUsage: value?.bizWalletOffsetUsage ?? "UNKNOWN",
    fastSettlementUsage: value?.fastSettlementUsage ?? "UNKNOWN",
    claimIncluded: value?.claimIncluded ?? "UNKNOWN",
  };
}

export default function SettlementContextForm({
  value,
  highlight = false,
  onChange,
}: Props) {
  const currentValue = normalizeContext(value);

  function handleOptionChange(key: OptionKey, nextValue: AnalysisOptionValue) {
    onChange({
      ...currentValue,
      [key]: nextValue,
    });
  }

  return (
    <div
      className={`rounded-xl border bg-white p-4 ${
        highlight ? "border-orange-300 ring-1 ring-orange-200" : "border-slate-200"
      }`}
    >
      <h3 className="mb-2 text-sm font-semibold">정책성 차이 확인 옵션</h3>

      <p className="mb-4 text-xs leading-5 text-slate-500">
        기본 파일 3개만으로는 확인하기 어려운 정책성 요소입니다.
        <br />
        선택값은 백엔드 재분석 시 explainable / pending / confirmed 판정 보조값으로 사용됩니다.
      </p>

      <div className="space-y-3">
        {optionConfigs.map((option) => (
          <div
            key={option.key}
            className="rounded-lg border border-slate-200 p-3"
          >
            <p className="text-sm font-medium">{option.label}</p>
            <p className="mt-1 text-xs leading-5 text-slate-500">
              {option.description}
            </p>

            <div className="mt-3 flex flex-wrap gap-3 text-sm">
              {OPTION_CHOICES.map((choice) => {
                const checked = currentValue[option.key] === choice;

                return (
                  <label
                    key={choice}
                    className={`flex cursor-pointer items-center gap-2 rounded-full border px-3 py-2 transition ${
                      checked
                        ? "border-black bg-black text-white"
                        : "border-slate-300 bg-white text-slate-700 hover:border-slate-400"
                    }`}
                  >
                    <input
                      type="radio"
                      name={option.key}
                      className="hidden"
                      checked={checked}
                      onChange={() => handleOptionChange(option.key, choice)}
                    />
                    <span>
                      {choice === "YES"
                        ? "예"
                        : choice === "NO"
                          ? "아니오"
                          : "모름"}
                    </span>
                  </label>
                );
              })}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}