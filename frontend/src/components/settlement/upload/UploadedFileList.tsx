import type { UploadedFileItem } from "../../../types/settlementUi";
import { FILE_TYPE_LABEL } from "../../../utils/settlementLabels";

type Props = {
  files: UploadedFileItem[];
};

function formatBytes(bytes: number) {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}

export default function UploadedFileList({ files }: Props) {
  return (
    <div className="rounded-xl border bg-white p-4">
      <h3 className="mb-3 text-sm font-semibold">업로드 파일</h3>

      {files.length === 0 ? (
        <div className="text-sm text-slate-500">
          아직 업로드한 파일이 없습니다.
        </div>
      ) : (
        <div className="max-h-56 space-y-2 overflow-y-auto pr-1">
          {files.map((file) => (
            <div
              key={file.id}
              className="rounded-lg border px-3 py-3 text-sm"
            >
              <div className="flex items-start justify-between gap-3">
                <div className="min-w-0 flex-1">
                  <div className="mb-1 flex items-center gap-2">
                    <span className="rounded-full bg-slate-100 px-2 py-1 text-xs text-slate-700">
                      {FILE_TYPE_LABEL[file.fileType]}
                    </span>
                    <span
                      className={`rounded-full px-2 py-1 text-xs ${
                        file.status === "CONNECTED"
                          ? "bg-green-100 text-green-700"
                          : file.status === "UPLOADING"
                            ? "bg-blue-100 text-blue-700"
                            : "bg-red-100 text-red-700"
                      }`}
                    >
                      {file.status === "CONNECTED"
                        ? "연결 완료"
                        : file.status === "UPLOADING"
                          ? "업로드 중"
                          : "실패"}
                    </span>
                  </div>

                  <div className="truncate font-medium">{file.fileName}</div>
                  <div className="mt-1 text-xs text-slate-500">
                    {formatBytes(file.size)} · {file.uploadedAt}
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}