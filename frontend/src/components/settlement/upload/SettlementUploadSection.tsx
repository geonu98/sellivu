import { useRef, useState } from "react";
import type { SettlementFileType } from "../../../types/settlementAnalysis";
import { FILE_TYPE_LABEL } from "../../../utils/settlementLabels";

type Props = {
  disabled?: boolean;
  onUpload: (file: File, fileType: SettlementFileType) => Promise<void>;
};

const FILE_TYPES: SettlementFileType[] = [
  "DAILY_SETTLEMENT",
  "ORDER_SETTLEMENT",
  "FEE_DETAIL",
];

export default function SettlementUploadSection({
  disabled = false,
  onUpload,
}: Props) {
  const [loadingType, setLoadingType] = useState<SettlementFileType | null>(null);
  const [dragActiveType, setDragActiveType] = useState<SettlementFileType | null>(null);

  const inputRefs = useRef<Record<SettlementFileType, HTMLInputElement | null>>({
    DAILY_SETTLEMENT: null,
    ORDER_SETTLEMENT: null,
    FEE_DETAIL: null,
  });

  async function handleFile(file: File, fileType: SettlementFileType) {
    setLoadingType(fileType);
    try {
      await onUpload(file, fileType);
    } finally {
      setLoadingType(null);
    }
  }

  async function handleChange(
    e: React.ChangeEvent<HTMLInputElement>,
    fileType: SettlementFileType
  ) {
    const file = e.target.files?.[0];
    if (!file) return;

    await handleFile(file, fileType);
    e.target.value = "";
  }

  async function handleDrop(
    e: React.DragEvent<HTMLDivElement>,
    fileType: SettlementFileType
  ) {
    e.preventDefault();
    setDragActiveType(null);

    const file = e.dataTransfer.files?.[0];
    if (!file || disabled || loadingType) return;

    await handleFile(file, fileType);
  }

  return (
    <div className="rounded-2xl border border-slate-200 bg-white p-3.5">
      <div className="space-y-3">
        {FILE_TYPES.map((fileType) => {
          const dragActive = dragActiveType === fileType;
          const loading = loadingType === fileType;

          return (
            <div
              key={fileType}
              onDragOver={(e) => {
                e.preventDefault();
                if (!disabled && !loadingType) {
                  setDragActiveType(fileType);
                }
              }}
              onDragLeave={() => setDragActiveType(null)}
              onDrop={(e) => void handleDrop(e, fileType)}
              onClick={() => {
                if (!disabled && !loadingType) {
                  inputRefs.current[fileType]?.click();
                }
              }}
              className={`cursor-pointer rounded-2xl border border-dashed px-4 py-4 transition ${
                dragActive
                  ? "border-blue-500 bg-blue-50"
                  : "border-slate-300 bg-slate-50 hover:border-slate-400 hover:bg-slate-100"
              } ${disabled ? "cursor-not-allowed opacity-60" : ""}`}
            >
              <input
                ref={(el) => {
                  inputRefs.current[fileType] = el;
                }}
                type="file"
                className="hidden"
                disabled={disabled || loadingType !== null}
                onChange={(e) => void handleChange(e, fileType)}
              />

              <div className="flex items-center gap-3.5">
                <div className="flex h-11 w-11 shrink-0 items-center justify-center rounded-full bg-white text-xl font-bold text-slate-700 shadow-sm">
                  +
                </div>

                <div className="min-w-0 flex-1">
                  <div className="text-[13px] font-bold text-slate-800">
                    {FILE_TYPE_LABEL[fileType]}
                  </div>
                  <div className="mt-1 text-[11px] leading-4 text-slate-500">
                    클릭/드래그해서 파일 추가
                  </div>
                </div>

                {loading && (
                  <div className="shrink-0 rounded-full bg-blue-100 px-2.5 py-1 text-[11px] font-bold text-blue-700">
                    업로드 중
                  </div>
                )}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}