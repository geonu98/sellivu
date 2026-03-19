import type { WorkspaceFileResponse } from "../../../types/settlementAnalysis";

type Props = {
  items: WorkspaceFileResponse[];
  removing: boolean;
  onRemove: (workspaceFileId: number) => Promise<void>;
};

export default function UploadedFileListCard({
  items,
  removing,
  onRemove,
}: Props) {
  if (items.length === 0) {
    return (
      <div className="rounded-xl border border-dashed border-slate-200 bg-white p-3 text-xs text-slate-500">
        아직 업로드된 파일이 없습니다.
      </div>
    );
  }

  return (
    <div className="space-y-2">
      {items.map((item) => (
        <div
          key={item.workspaceFileId}
          className="rounded-xl border border-slate-200 bg-white px-3 py-2.5"
        >
          <div className="flex items-start justify-between gap-3">
            <div className="min-w-0 flex-1">
              <div className="truncate text-sm font-semibold text-slate-900">
                {item.originalFileName}
              </div>
              <div className="mt-1 text-[11px] text-slate-500">
                {item.fileType}
              </div>
              <div className="mt-0.5 text-[11px] text-slate-400">
                {new Date(item.createdAt).toLocaleString("ko-KR")}
              </div>
            </div>

            <div className="flex shrink-0 flex-col items-end gap-1.5">
              <span className="rounded-full bg-green-100 px-2 py-0.5 text-[10px] font-bold text-green-700">
                CONNECTED
              </span>
              <button
                type="button"
                onClick={() => onRemove(item.workspaceFileId)}
                disabled={removing}
                className="rounded-lg border border-slate-200 px-2.5 py-1 text-[11px] font-medium text-slate-700 disabled:opacity-50"
              >
                제거
              </button>
            </div>
          </div>
        </div>
      ))}
    </div>
  );
}