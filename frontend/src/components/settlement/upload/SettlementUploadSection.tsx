import { useRef, useState } from "react";
import type { SettlementFileType } from "../../../types/settlementAnalysis";
import { FILE_TYPE_LABEL } from "../../../utils/settlementLabels";

type Props = {
  disabled?: boolean;
  uploadingFileType?: SettlementFileType | null;
  runningFileType?: SettlementFileType | null;
  onUpload: (file: File, fileType: SettlementFileType) => Promise<void>;
};

const FILE_TYPES: SettlementFileType[] = [
  "DAILY_SETTLEMENT",
  "ORDER_SETTLEMENT",
  "FEE_DETAIL",
];

export default function SettlementUploadSection({
  disabled = false,
  uploadingFileType = null,
  runningFileType = null,
  onUpload,
}: Props) {
  const [dragActiveType, setDragActiveType] = useState<SettlementFileType | null>(null);

  const inputRefs = useRef<Record<SettlementFileType, HTMLInputElement | null>>({
    DAILY_SETTLEMENT: null,
    ORDER_SETTLEMENT: null,
    FEE_DETAIL: null,
  });

  async function handleFile(file: File, fileType: SettlementFileType) {
    console.log("handleFile start", { fileName: file.name, fileType });
    try {
      await onUpload(file, fileType);
      console.log("handleFile success");
    } catch (error) {
      console.error("handleFile error", error);
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
    if (!file || disabled || uploadingFileType || runningFileType) return;

    await handleFile(file, fileType);
  }

  const hasBusyState = uploadingFileType !== null || runningFileType !== null;

  return (
    <div className="flex h-full flex-col gap-4">
      {FILE_TYPES.map((fileType) => {
        const dragActive = dragActiveType === fileType;
        const uploading = uploadingFileType === fileType;
        const running = runningFileType === fileType;

        return (
          <div
            key={fileType}
            onDragOver={(e) => {
              e.preventDefault();
              if (!disabled && !hasBusyState) {
                setDragActiveType(fileType);
              }
            }}
            onDragLeave={() => setDragActiveType(null)}
            onDrop={(e) => void handleDrop(e, fileType)}
            onClick={() => {
              if (!disabled && !hasBusyState) {
                inputRefs.current[fileType]?.click();
              }
            }}
            className={`flex min-h-[110px] flex-1 cursor-pointer items-center rounded-2xl border border-dashed px-4 py-4 transition ${
              dragActive
                ? "border-blue-500 bg-blue-50"
                : "border-slate-300 bg-white hover:border-slate-400 hover:bg-slate-50"
            } ${disabled || hasBusyState ? "cursor-not-allowed" : ""} ${
              disabled ? "opacity-60" : ""
            }`}
          >
            <input
              ref={(el) => {
                inputRefs.current[fileType] = el;
              }}
              type="file"
              className="hidden"
              disabled={disabled || hasBusyState}
              onChange={(e) => void handleChange(e, fileType)}
            />

            <div className="flex w-full items-center gap-4">
              <div className="flex h-14 w-14 shrink-0 items-center justify-center rounded-full bg-slate-50 text-2xl font-bold text-slate-700 shadow-sm">
                +
              </div>

              <div className="min-w-0 flex-1">
                <div className="text-[15px] font-bold text-slate-800">
                  {FILE_TYPE_LABEL[fileType]}
                </div>
                <div className="mt-1 text-[12px] leading-5 text-slate-500">
                  클릭/드래그해서 업로드
                </div>
              </div>

              {uploading && (
                <div className="shrink-0 rounded-full bg-blue-100 px-3 py-1.5 text-[11px] font-bold text-blue-700">
                  업로드 중
                </div>
              )}

              {!uploading && running && (
                <div className="shrink-0 rounded-full bg-amber-100 px-3 py-1.5 text-[11px] font-bold text-amber-700">
                  분석 중
                </div>
              )}
            </div>
          </div>
        );
      })}
    </div>
  );
}