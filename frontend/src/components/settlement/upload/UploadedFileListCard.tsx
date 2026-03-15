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
  return (
    <div className="rounded-xl border bg-white p-4">
      <div className="mb-3">
        <h2 className="text-sm font-semibold text-slate-900">업로드된 파일</h2>
        <p className="mt-1 text-xs text-slate-500">
          현재 워크스페이스에서 활성 상태로 연결된 파일 목록입니다.
        </p>
      </div>

      {items.length === 0 ? (
        <div className="rounded-lg bg-slate-50 p-3 text-xs text-slate-500">
          아직 업로드된 파일이 없습니다.
        </div>
      ) : (
        <div className="space-y-2">
          {items.map((item) => (
            <div
              key={item.workspaceFileId}
              className="rounded-lg border border-slate-200 p-3"
            >
              <div className="flex items-start justify-between gap-3">
                <div className="min-w-0">
                  <div className="truncate text-sm font-medium text-slate-900">
                    {item.originalFileName}
                  </div>
                  <div className="mt-1 text-xs text-slate-500">
                    {item.fileType}
                  </div>
                  <div className="mt-1 text-xs text-slate-500">
                    연결 시각: {new Date(item.createdAt).toLocaleString("ko-KR")}
                  </div>
                </div>

                <div className="flex flex-col items-end gap-2">
                  <span className="rounded-full bg-green-100 px-2 py-1 text-[11px] font-medium text-green-700">
                    CONNECTED
                  </span>
                  <button
                    type="button"
                    onClick={() => onRemove(item.workspaceFileId)}
                    disabled={removing}
                    className="rounded-lg border px-2 py-1 text-xs text-slate-700 disabled:opacity-50"
                  >
                    제거
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}