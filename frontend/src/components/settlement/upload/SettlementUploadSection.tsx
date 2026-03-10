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
    <div className="rounded-xl border bg-white p-4">
      <div className="mb-3">
        <h3 className="text-sm font-semibold">파일 업로드</h3>
        <p className="mt-1 text-xs text-slate-500">
          정산 파일을 드래그하거나 클릭해서 추가하세요.
        </p>
      </div>

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
              className={`cursor-pointer rounded-2xl border-2 border-dashed p-5 transition ${
                dragActive
                  ? "border-blue-500 bg-blue-50"
                  : "border-slate-300 bg-slate-50 hover:border-slate-400"
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

              <div className="flex flex-col items-center justify-center text-center">
                <div className="mb-3 flex h-12 w-12 items-center justify-center rounded-full bg-white text-2xl font-bold shadow-sm">
                  +
                </div>

                <div className="text-sm font-semibold">
                  {FILE_TYPE_LABEL[fileType]}
                </div>

                <div className="mt-1 text-xs text-slate-500">
                  파일을 드래그하거나 클릭하여 추가하세요
                </div>

                {loading && (
                  <div className="mt-3 rounded-full bg-blue-100 px-3 py-1 text-xs text-blue-700">
                    업로드 및 연결 중...
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