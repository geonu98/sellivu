import type {
  AnalysisCapabilityResponse,
  AnalysisContextResponse,
  AnalysisSetItemResponse,
  AnalysisSetResponse,
  SettlementFileType,
  UpdateAnalysisContextRequest,
} from "../../../types/settlementAnalysis";
import type { UploadedFileItem } from "../../../types/settlementUi";
import AnalysisSetCreateForm from "../analysis-set/AnalysisSetCreateForm";
import AnalysisSetList from "../analysis-set/AnalysisSetList";
import AnalysisSetItemsCard from "../analysis-set/AnalysisSetItemsCard";
import SettlementUploadSection from "../upload/SettlementUploadSection";
import UploadedFileList from "../upload/UploadedFileList";
import SettlementContextForm from "../context/SettlementContextForm";

type Props = {
  analysisSets: AnalysisSetResponse[];
  selectedAnalysisSetId: number | null;
  items: AnalysisSetItemResponse[];
  context: AnalysisContextResponse | null;
  capability: AnalysisCapabilityResponse | null;
  uploadedFiles: UploadedFileItem[];
  contextSaving: boolean;
  rebuildLoading: boolean;
  onCreateAnalysisSet: (name: string) => Promise<void>;
  onSelectAnalysisSet: (id: number) => void;
  onUploadFile: (file: File, fileType: SettlementFileType) => Promise<void>;
  onChangeContext: (next: UpdateAnalysisContextRequest) => void;
  onSaveContext: () => Promise<void>;
  onRebuild: () => Promise<void>;
};

export default function SettlementLeftPanel({
  analysisSets,
  selectedAnalysisSetId,
  items,
  context,
  capability,
  uploadedFiles,
  contextSaving,
  rebuildLoading,
  onCreateAnalysisSet,
  onSelectAnalysisSet,
  onUploadFile,
  onChangeContext,
  onSaveContext,
  onRebuild,
}: Props) {
  const hasSelectedSet = selectedAnalysisSetId !== null;

  return (
    <div className="space-y-4">
      <AnalysisSetCreateForm onCreate={onCreateAnalysisSet} />

      <AnalysisSetList
        analysisSets={analysisSets}
        selectedAnalysisSetId={selectedAnalysisSetId}
        onSelect={onSelectAnalysisSet}
      />

      <SettlementUploadSection
        disabled={!hasSelectedSet}
        onUpload={onUploadFile}
      />

      <UploadedFileList files={uploadedFiles} />

      <AnalysisSetItemsCard items={items} />

      <SettlementContextForm
        value={context}
        saving={contextSaving}
        highlight={capability?.requiresContextOptions ?? false}
        onChange={onChangeContext}
        onSave={onSaveContext}
      />

      <div className="rounded-xl border bg-white p-4">
        <button
          onClick={onRebuild}
          disabled={!hasSelectedSet || rebuildLoading}
          className="w-full rounded-lg bg-black px-4 py-3 text-sm text-white disabled:opacity-50"
        >
          {rebuildLoading ? "분석 실행 중..." : "분석 실행 / 재분석"}
        </button>
      </div>
    </div>
  );
}