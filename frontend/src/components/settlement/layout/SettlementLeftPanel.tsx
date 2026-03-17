import type {
  AnalysisCapabilityResponse,
  AnalysisContextResponse,
  SettlementFileType,
  UpdateAnalysisContextRequest,
  WorkspaceFileResponse,
  WorkspaceStatus,
} from "../../../types/settlementAnalysis";
import SettlementUploadSection from "../upload/SettlementUploadSection";
import SettlementContextForm from "../context/SettlementContextForm";
import UploadedFileListCard from "../upload/UploadedFileListCard";

type Props = {
  context: AnalysisContextResponse | null;
  capability: AnalysisCapabilityResponse | null;
  workspaceFiles: WorkspaceFileResponse[];
  contextSaving: boolean;
  saveLoading: boolean;
  removingFile: boolean;
  isAuthenticated: boolean;
  workspaceStatus: WorkspaceStatus | null;
  onUploadFile: (file: File, fileType: SettlementFileType) => Promise<void>;
  onRemoveFile: (workspaceFileId: number) => Promise<void>;
  onChangeContext: (next: UpdateAnalysisContextRequest) => void;
  onSaveContext: () => Promise<void>;
  onSaveWorkspace: () => Promise<void>;
};

export default function SettlementLeftPanel({
  context,
  capability,
  workspaceFiles,
  contextSaving,
  saveLoading,
  removingFile,
  isAuthenticated,
  workspaceStatus,
  onUploadFile,
  onRemoveFile,
  onChangeContext,
  onSaveContext,
  onSaveWorkspace,
}: Props) {
  const isActiveWorkspace = workspaceStatus === "ACTIVE";
  const hasConnectedFiles = workspaceFiles.length > 0;

  const canSaveWorkspace =
    isAuthenticated && isActiveWorkspace && hasConnectedFiles;

  return (
    <aside className="flex h-full flex-col rounded-2xl border border-slate-200 bg-white shadow-sm">
      <div className="border-b border-slate-100 px-5 py-4">
        <p className="text-xs font-semibold uppercase tracking-[0.12em] text-slate-400">
          Workspace
        </p>
        <h2 className="mt-1 text-lg font-semibold text-slate-900">
          정산 분석 준비
        </h2>
        <p className="mt-1 text-sm leading-6 text-slate-500">
          업로드 파일과 분석 조건을 먼저 정리한 뒤, 현재 워크스페이스를 저장합니다.
        </p>
      </div>

      <div className="flex-1 space-y-5 overflow-y-auto px-5 py-5">
        <section className="rounded-2xl border border-slate-200 bg-slate-50 p-4">
          <div className="flex items-start justify-between gap-3">
            <div>
              <h3 className="text-sm font-semibold text-slate-900">
                업로드 가이드
              </h3>
              <p className="mt-1 text-xs leading-5 text-slate-500">
                일별 정산, 월별 정산, 주문, 수수료 파일을 연결해 분석 결과를 구성합니다.
              </p>
            </div>
            <span
              className={`rounded-full px-2.5 py-1 text-[11px] font-medium ${
                isActiveWorkspace
                  ? "bg-emerald-100 text-emerald-700"
                  : "bg-amber-100 text-amber-700"
              }`}
            >
              {workspaceStatus ?? "UNKNOWN"}
            </span>
          </div>
        </section>

        <section className="space-y-3">
          <div>
            <h3 className="text-sm font-semibold text-slate-900">
              파일 업로드
            </h3>
            <p className="mt-1 text-xs text-slate-500">
              분석에 필요한 파일을 워크스페이스에 추가하세요.
            </p>
          </div>

          <div className="rounded-2xl border border-slate-200 bg-white p-4">
            <SettlementUploadSection
              disabled={!isActiveWorkspace}
              onUpload={onUploadFile}
            />
          </div>
        </section>

        <section className="space-y-3">
          <div className="flex items-center justify-between">
            <div>
              <h3 className="text-sm font-semibold text-slate-900">
                업로드된 파일
              </h3>
              <p className="mt-1 text-xs text-slate-500">
                현재 분석에 연결된 파일 목록입니다.
              </p>
            </div>
            <span className="rounded-full bg-slate-100 px-2.5 py-1 text-xs font-medium text-slate-600">
              {workspaceFiles.length}개
            </span>
          </div>

          <div className="rounded-2xl border border-slate-200 bg-white p-4">
            <UploadedFileListCard
              items={workspaceFiles}
              removing={removingFile}
              onRemove={onRemoveFile}
            />
          </div>
        </section>

        <section className="space-y-3">
          <div>
            <h3 className="text-sm font-semibold text-slate-900">
              체크 항목 / 분석 조건
            </h3>
            <p className="mt-1 text-xs text-slate-500">
              비교 기준이나 필수 입력값을 설정합니다.
            </p>
          </div>

          <div
            className={`rounded-2xl border bg-white p-4 ${
              capability?.requiresContextOptions
                ? "border-amber-300 ring-2 ring-amber-100"
                : "border-slate-200"
            }`}
          >
            <SettlementContextForm
              value={context}
              saving={contextSaving}
              highlight={capability?.requiresContextOptions ?? false}
              onChange={onChangeContext}
              onSave={onSaveContext}
            />
          </div>
        </section>
      </div>

      <div className="border-t border-slate-100 px-5 py-4">
        <div className="rounded-2xl bg-slate-900 p-4">
          <div className="mb-3">
            <h3 className="text-sm font-semibold text-white">
              현재 분석 저장
            </h3>
            <p className="mt-1 text-xs leading-5 text-slate-300">
              지금 구성한 워크스페이스를 저장해 나중에 다시 불러올 수 있습니다.
            </p>
          </div>

          <button
            type="button"
            onClick={onSaveWorkspace}
            disabled={!canSaveWorkspace || saveLoading}
            className="w-full rounded-xl bg-white px-4 py-3 text-sm font-semibold text-slate-900 transition hover:bg-slate-100 disabled:cursor-not-allowed disabled:opacity-50"
          >
            {saveLoading ? "저장 중..." : "현재 분석 저장"}
          </button>

          <div className="mt-3 space-y-1.5">
            {!isAuthenticated && (
              <p className="text-xs text-slate-300">
                로그인하면 현재 워크스페이스를 저장하고 다시 조회할 수 있습니다.
              </p>
            )}

            {isAuthenticated && !hasConnectedFiles && (
              <p className="text-xs text-slate-300">
                저장하려면 최소 1개 이상의 업로드 파일이 필요합니다.
              </p>
            )}

            {workspaceStatus && workspaceStatus !== "ACTIVE" && (
              <p className="text-xs text-slate-300">
                현재 워크스페이스 상태가 {workspaceStatus} 이므로 저장이 제한될 수
                있습니다.
              </p>
            )}
          </div>
        </div>
      </div>
    </aside>
  );
}