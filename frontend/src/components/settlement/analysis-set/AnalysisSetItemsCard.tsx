import type {
  AnalysisSetItemResponse,
  SettlementFileType,
} from "../../../types/settlementAnalysis";
import { FILE_TYPE_LABEL } from "../../../utils/settlementLabels";

type Props = {
  items: AnalysisSetItemResponse[];
};

const FILE_TYPES: SettlementFileType[] = [
  "DAILY_SETTLEMENT",
  "ORDER_SETTLEMENT",
  "FEE_DETAIL",
];

export default function AnalysisSetItemsCard({ items }: Props) {
  const hasType = (fileType: SettlementFileType) =>
    items.some((item) => item.fileType === fileType);

  return (
    <div className="rounded-xl border bg-white p-4">
      <h3 className="mb-3 text-sm font-semibold">연결된 파일 상태</h3>
      <div className="space-y-2">
        {FILE_TYPES.map((fileType) => {
          const connected = hasType(fileType);

          return (
            <div
              key={fileType}
              className="flex items-center justify-between rounded-lg border px-3 py-2 text-sm"
            >
              <span>{FILE_TYPE_LABEL[fileType]}</span>
              <span
                className={
                  connected
                    ? "rounded-full bg-green-100 px-2 py-1 text-xs text-green-700"
                    : "rounded-full bg-slate-100 px-2 py-1 text-xs text-slate-600"
                }
              >
                {connected ? "연결됨" : "미연결"}
              </span>
            </div>
          );
        })}
      </div>
    </div>
  );
}