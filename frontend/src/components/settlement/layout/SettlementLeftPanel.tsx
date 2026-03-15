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
    <div className="space-y-4">
      <div className="rounded-xl border bg-white p-4">
        <h2 className="text-sm font-semibold text-slate-900">
          워크스페이스 업로드
        </h2>
        <p className="mt-1 text-xs text-slate-500">
          일별 정산, 건별 정산, 수수료 상세 파일을 업로드해 현재 워크스페이스 기준으로 분석합니다.
        </p>
      </div>

      <SettlementUploadSection
        disabled={!isActiveWorkspace}
        onUpload={onUploadFile}
      />

      <UploadedFileListCard
        items={workspaceFiles}
        removing={removingFile}
        onRemove={onRemoveFile}
      />

      <SettlementContextForm
        value={context}
        saving={contextSaving}
        highlight={capability?.requiresContextOptions ?? false}
        onChange={onChangeContext}
        onSave={onSaveContext}
      />

      <div className="rounded-xl border bg-white p-4">
        <div className="space-y-2">
          <button
            type="button"
            onClick={onSaveWorkspace}
            disabled={!canSaveWorkspace || saveLoading}
            className="w-full rounded-lg bg-black px-4 py-3 text-sm text-white disabled:opacity-50"
          >
            {saveLoading ? "저장 중..." : "현재 분석 저장"}
          </button>

          {!isAuthenticated && (
            <p className="text-xs text-slate-500">
              로그인하면 현재 워크스페이스를 저장하고, 나중에 다시 조회할 수 있습니다.
            </p>
          )}

          {isAuthenticated && !hasConnectedFiles && (
            <p className="text-xs text-slate-500">
              저장하려면 최소 1개 이상의 업로드 파일이 필요합니다.
            </p>
          )}

          {workspaceStatus && workspaceStatus !== "ACTIVE" && (
            <p className="text-xs text-slate-500">
              현재 워크스페이스 상태가 {workspaceStatus} 이므로 저장이 제한될 수 있습니다.
            </p>
          )}
        </div>
      </div>
    </div>
  );
}