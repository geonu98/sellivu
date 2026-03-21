import { useEffect, useMemo, useState, type ReactNode } from "react";
import {
  createWorkspace,
  fetchWorkspace,
  fetchWorkspaceDailyRows,
  fetchWorkspaceFeeRows,
  fetchWorkspaceIssues,
  fetchWorkspaceMonthlyRows,
  fetchWorkspaceOrderRows,
  fetchWorkspaceSnapshots,
  removeWorkspaceFile,
  saveWorkspace,
  updateWorkspaceContext,
  uploadWorkspaceFile,
} from "../../api/settlementWorkspaceApi";
import {
  fetchAnalysisSets,
  restoreAnalysisSetToWorkspace,
} from "../../api/settlementAnalysisSetApi";
import { authApi } from "../../api/authApi";
import SettlementUploadSection from "../../components/settlement/upload/SettlementUploadSection";
import UploadedFileListCard from "../../components/settlement/upload/UploadedFileListCard";
import SettlementContextForm from "../../components/settlement/context/SettlementContextForm";
import SettlementResultTabs from "../../components/settlement/tabs/SettlementResultTabs";
import IssuesTable from "../../components/settlement/tabs/IssuesTable";
import SnapshotsTable from "../../components/settlement/tabs/SnapshotsTable";
import DailyTable from "../../components/settlement/tabs/DailyTable";
import MonthlyTable from "../../components/settlement/tabs/MonthlyTable";
import OrdersTable from "../../components/settlement/tabs/OrdersTable";
import FeesTable from "../../components/settlement/tabs/FeesTable";
import type {
  AnalysisCapabilityResponse,
  AnalysisContextResponse,
  AnalysisSetResponse,
  DailyRow,
  FeeRow,
  IssueRow,
  MonthlyRow,
  OrderRow,
  SettlementFileType,
  SnapshotRow,
  UpdateAnalysisContextRequest,
  WorkspaceResponse,
  WorkspaceSession,
} from "../../types/settlementAnalysis";
import { buildTabs, type TabKey } from "../../utils/settlementTab";
import { getApiErrorMessage } from "../../utils/apiError";
import { useAuthStore } from "../../store/authStore";

const WORKSPACE_SESSION_KEY = "sellivu-workspace-session";

function loadWorkspaceSession(): WorkspaceSession | null {
  const raw = sessionStorage.getItem(WORKSPACE_SESSION_KEY);
  if (!raw) return null;

  try {
    return JSON.parse(raw) as WorkspaceSession;
  } catch {
    return null;
  }
}

function saveWorkspaceSession(session: WorkspaceSession) {
  sessionStorage.setItem(WORKSPACE_SESSION_KEY, JSON.stringify(session));
}

function clearWorkspaceSession() {
  sessionStorage.removeItem(WORKSPACE_SESSION_KEY);
}

function createDefaultContext(): UpdateAnalysisContextRequest {
  return {
    storeCouponUsage: "UNKNOWN",
    naverCouponUsage: "UNKNOWN",
    pointBenefitUsage: "UNKNOWN",
    safeReturnCareUsage: "UNKNOWN",
    bizWalletOffsetUsage: "UNKNOWN",
    fastSettlementUsage: "UNKNOWN",
    claimIncluded: "UNKNOWN",
  };
}

function normalizeContext(
  value: AnalysisContextResponse | null | undefined
): UpdateAnalysisContextRequest {
  return {
    storeCouponUsage: value?.storeCouponUsage ?? "UNKNOWN",
    naverCouponUsage: value?.naverCouponUsage ?? "UNKNOWN",
    pointBenefitUsage: value?.pointBenefitUsage ?? "UNKNOWN",
    safeReturnCareUsage: value?.safeReturnCareUsage ?? "UNKNOWN",
    bizWalletOffsetUsage: value?.bizWalletOffsetUsage ?? "UNKNOWN",
    fastSettlementUsage: value?.fastSettlementUsage ?? "UNKNOWN",
    claimIncluded: value?.claimIncluded ?? "UNKNOWN",
  };
}

function EmptyState({
  title,
  description,
}: {
  title: string;
  description: string;
}) {
  return (
    <div className="flex h-full min-h-[260px] flex-col items-center justify-center rounded-[24px] border border-dashed border-slate-200 bg-slate-50 px-6 py-10 text-center">
      <div className="mb-4 flex h-16 w-16 items-center justify-center rounded-full bg-slate-100">
        <EmptyBoxIcon className="h-8 w-8 text-slate-400" />
      </div>
      <p className="text-base font-semibold text-slate-700">{title}</p>
      <p className="mt-1 text-sm leading-6 text-slate-500">{description}</p>
    </div>
  );
}

function KpiCard({
  label,
  value,
  tone = "blue",
  icon,
}: {
  label: string;
  value: number;
  tone?: "blue" | "emerald" | "amber" | "rose";
  icon: ReactNode;
}) {
  const toneClass = {
    blue: {
      wrap: "bg-blue-50 text-blue-700 border-blue-100",
      icon: "bg-blue-100 text-blue-700",
      value: "text-blue-700",
    },
    emerald: {
      wrap: "bg-emerald-50 text-emerald-700 border-emerald-100",
      icon: "bg-emerald-100 text-emerald-700",
      value: "text-emerald-700",
    },
    amber: {
      wrap: "bg-amber-50 text-amber-700 border-amber-100",
      icon: "bg-amber-100 text-amber-700",
      value: "text-amber-700",
    },
    rose: {
      wrap: "bg-rose-50 text-rose-700 border-rose-100",
      icon: "bg-rose-100 text-rose-700",
      value: "text-rose-700",
    },
  }[tone];

  return (
    <div className={`rounded-[20px] border p-3.5 shadow-sm ${toneClass.wrap}`}>
      <div className="flex items-start justify-between gap-3">
        <div>
          <p className="text-[12px] font-semibold">{label}</p>
          <p className={`mt-2 text-[30px] font-black ${toneClass.value}`}>
            {value}
          </p>
        </div>
        <div
          className={`flex h-10 w-10 items-center justify-center rounded-full ${toneClass.icon}`}
        >
          {icon}
        </div>
      </div>
    </div>
  );
}

function SummaryActionCard({
  title,
  count,
  description,
  buttonLabel,
  onClick,
  tone = "slate",
}: {
  title: string;
  count: number;
  description: string;
  buttonLabel: string;
  onClick: () => void;
  tone?: "blue" | "slate";
}) {
  const toneClass =
    tone === "blue"
      ? {
          wrap: "border-blue-200 bg-blue-50/70",
          badge: "bg-blue-100 text-blue-700",
          button: "bg-blue-600 text-white hover:bg-blue-700",
        }
      : {
          wrap: "border-slate-200 bg-slate-50/70",
          badge: "bg-white text-slate-700 border border-slate-200",
          button:
            "bg-white text-slate-700 border border-slate-200 hover:bg-slate-50",
        };

  return (
    <div className={`rounded-[16px] border p-2 ${toneClass.wrap}`}>
      <div className="flex items-start justify-between gap-3">
        <div className="min-w-0">
          <p className="text-[10px] font-extrabold uppercase tracking-[0.16em] text-slate-400">
            {title}
          </p>

          <div className="mt-1.5 flex items-center gap-2">
            <span
              className={`rounded-full px-2 py-0.5 text-[10px] font-bold ${toneClass.badge}`}
            >
              {count}개
            </span>
          </div>

          <p className="mt-1 text-[11px] leading-4 text-slate-500">
            {description}
          </p>
        </div>
      </div>

      <button
        type="button"
        onClick={onClick}
        className={`mt-2 w-full rounded-lg px-3 py-1.5 text-[10px] font-bold transition ${toneClass.button}`}
      >
        {buttonLabel}
      </button>
    </div>
  );
}

function FileTypeLabel(type: string) {
  switch (type) {
    case "DAILY":
      return "일별 정산";
    case "MONTHLY":
      return "월별 정산";
    case "ORDER_DETAIL":
      return "건별 정산";
    case "FEE_DETAIL":
      return "수수료 상세";
    case "ORDER_FEE_CROSS_CHECK":
      return "교차 검증";
    case "ISSUES":
      return "이슈 리포트";
    default:
      return type;
  }
}

function OptionValueLabel(value: string) {
  switch (value) {
    case "YES":
      return "예";
    case "NO":
      return "아니오";
    case "UNKNOWN":
      return "모름";
    default:
      return value;
  }
}

function OptionBadge({ value }: { value: string }) {
  const style =
    value === "YES"
      ? "bg-emerald-50 text-emerald-700 border-emerald-100"
      : value === "NO"
      ? "bg-rose-50 text-rose-700 border-rose-100"
      : "bg-slate-100 text-slate-600 border-slate-200";

  return (
    <span
      className={`inline-flex items-center rounded-full border px-2.5 py-1 text-[11px] font-bold ${style}`}
    >
      {OptionValueLabel(value)}
    </span>
  );
}

function CapabilityBadge({ label }: { label: string }) {
  return (
    <span className="inline-flex items-center rounded-full border border-slate-200 bg-white px-3 py-1 text-[11px] font-bold text-slate-600 shadow-sm">
      {label}
    </span>
  );
}

function SettlementContextSummaryCard({
  context,
  onEdit,
}: {
  context: UpdateAnalysisContextRequest;
  onEdit: () => void;
}) {
  const items = [
    { label: "스토어 쿠폰", value: context.storeCouponUsage },
    { label: "네이버 쿠폰", value: context.naverCouponUsage },
    { label: "포인트 혜택", value: context.pointBenefitUsage },
    { label: "안전반품 케어", value: context.safeReturnCareUsage },
    { label: "비즈월렛 상계", value: context.bizWalletOffsetUsage },
    { label: "빠른정산", value: context.fastSettlementUsage },
    { label: "클레임 포함", value: context.claimIncluded },
  ];

  return (
    <div className="flex h-full flex-col">
      <div className="mb-3">
        <p className="text-[12px] font-bold text-slate-700">현재 옵션 요약</p>
        <p className="mt-1 text-[11px] text-slate-500">
          분석에 반영될 옵션 상태입니다.
        </p>
      </div>

      <div className="grid grid-cols-1 gap-2">
        {items.map((item) => (
          <div
            key={item.label}
            className="flex items-center justify-between rounded-2xl border border-slate-200 bg-white px-3 py-3"
          >
            <span className="text-[12px] font-semibold text-slate-700">
              {item.label}
            </span>
            <OptionBadge value={item.value} />
          </div>
        ))}
      </div>

      <button
        type="button"
        onClick={onEdit}
        className="mt-3 w-full rounded-lg bg-slate-950 px-2.5 py-1.5 text-[11px] font-bold text-white transition hover:bg-slate-800"
      >
        옵션 편집
      </button>
    </div>
  );
}

function IssueIcon({ className = "h-5 w-5" }: { className?: string }) {
  return (
    <svg
      viewBox="0 0 24 24"
      fill="none"
      className={className}
      stroke="currentColor"
      strokeWidth="1.8"
    >
      <path d="M7 3.5h7l4 4V20a1 1 0 0 1-1 1H7a1 1 0 0 1-1-1v-15a1 1 0 0 1 1-1Z" />
      <path d="M14 3.5V8h4" />
      <path d="M9 12h6M9 16h6" strokeLinecap="round" />
    </svg>
  );
}

function CheckIcon({ className = "h-5 w-5" }: { className?: string }) {
  return (
    <svg
      viewBox="0 0 24 24"
      fill="none"
      className={className}
      stroke="currentColor"
      strokeWidth="1.8"
    >
      <circle cx="12" cy="12" r="9" />
      <path
        d="m8.5 12 2.5 2.5 4.5-5"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </svg>
  );
}

function SnapshotIcon({ className = "h-5 w-5" }: { className?: string }) {
  return (
    <svg
      viewBox="0 0 24 24"
      fill="none"
      className={className}
      stroke="currentColor"
      strokeWidth="1.8"
    >
      <path d="M4 12c2.5-5 13.5-5 16 0-2.5 5-13.5 5-16 0Z" />
      <circle cx="12" cy="12" r="2.5" />
    </svg>
  );
}

function AlertIcon({ className = "h-5 w-5" }: { className?: string }) {
  return (
    <svg
      viewBox="0 0 24 24"
      fill="none"
      className={className}
      stroke="currentColor"
      strokeWidth="1.8"
    >
      <path d="M12 8v5" strokeLinecap="round" />
      <circle cx="12" cy="16.5" r=".8" fill="currentColor" stroke="none" />
      <path d="M10.3 4.8 3.8 16A2 2 0 0 0 5.5 19h13a2 2 0 0 0 1.7-3l-6.5-11.2a2 2 0 0 0-3.4 0Z" />
    </svg>
  );
}

function EmptyBoxIcon({ className = "h-8 w-8" }: { className?: string }) {
  return (
    <svg
      viewBox="0 0 24 24"
      fill="none"
      className={className}
      stroke="currentColor"
      strokeWidth="1.7"
    >
      <path
        d="M4 10.5 12 6l8 4.5"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
      <path
        d="M4 10.5V18l8 4 8-4v-7.5"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
      <path d="M12 22v-8" strokeLinecap="round" />
    </svg>
  );
}

export default function SettlementAnalysisPage() {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  const isAuthInitialized = useAuthStore((state) => state.isAuthInitialized);
  const accessToken = useAuthStore((state) => state.accessToken);
  const user = useAuthStore((state) => state.user);
  const openAuthModal = useAuthStore((state) => state.openAuthModal);
  const clearAuth = useAuthStore((state) => state.clearAuth);

  const [workspaceSession, setWorkspaceSession] =
    useState<WorkspaceSession | null>(null);
  const [workspace, setWorkspace] = useState<WorkspaceResponse | null>(null);

  const [capability, setCapability] =
    useState<AnalysisCapabilityResponse | null>(null);
  const [context, setContext] = useState<AnalysisContextResponse | null>(null);

  const [issues, setIssues] = useState<IssueRow[]>([]);
  const [snapshots, setSnapshots] = useState<SnapshotRow[]>([]);
  const [dailyRows, setDailyRows] = useState<DailyRow[]>([]);
  const [monthlyRows, setMonthlyRows] = useState<MonthlyRow[]>([]);
  const [orderRows, setOrderRows] = useState<OrderRow[]>([]);
  const [feeRows, setFeeRows] = useState<FeeRow[]>([]);

  const [analysisSets, setAnalysisSets] = useState<AnalysisSetResponse[]>([]);
  const [analysisSetsLoading, setAnalysisSetsLoading] = useState(false);
  const [selectedAnalysisSetId, setSelectedAnalysisSetId] = useState<
    number | null
  >(null);
  const [isViewingSavedAnalysis, setIsViewingSavedAnalysis] = useState(false);

  const [activeTab, setActiveTab] = useState<TabKey>("OVERVIEW");
  const [isOptionEditorOpen, setIsOptionEditorOpen] = useState(false);
  const [isWorkspaceFilesOpen, setIsWorkspaceFilesOpen] = useState(false);
  const [isSavedListOpen, setIsSavedListOpen] = useState(false);

  const [pageLoading, setPageLoading] = useState(false);
  const [contextSaving, setContextSaving] = useState(false);
  const [saveLoading, setSaveLoading] = useState(false);
  const [removingFile, setRemovingFile] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [skipAutoInitOnce, setSkipAutoInitOnce] = useState(false);

  const [isRestoreConfirmOpen, setIsRestoreConfirmOpen] = useState(false);
  const [pendingRestoreAnalysisSetId, setPendingRestoreAnalysisSetId] =
    useState<number | null>(null);
  const [restoreAfterSaveLoading, setRestoreAfterSaveLoading] = useState(false);
  const [restoreWithoutSaveLoading, setRestoreWithoutSaveLoading] =
    useState(false);

  // 추가: 현재 작업이 저장되지 않은 상태인지 추적
  const [hasUnsavedWorkspaceChanges, setHasUnsavedWorkspaceChanges] =
    useState(false);

  const workspaceFiles = (workspace?.files ?? []).filter((file) => file.active);
  const isActiveWorkspace = workspace?.status === "ACTIVE";
  const hasConnectedFiles = workspaceFiles.length > 0;
  const canSaveWorkspace =
    isActiveWorkspace &&
    hasConnectedFiles &&
    !saveLoading &&
    !isViewingSavedAnalysis;

  const normalizedContext = useMemo(() => normalizeContext(context), [context]);

  async function loadWorkspaceResultData(
    workspaceKey: string,
    workspaceToken: string,
    capabilityRes?: AnalysisCapabilityResponse
  ) {
    const availableViews =
      capabilityRes?.availableViews ?? capability?.availableViews ?? [];

    const [issuesRes, snapshotsRes, dailyRes, monthlyRes, ordersRes, feesRes] =
      await Promise.all([
        availableViews.includes("ISSUES")
          ? fetchWorkspaceIssues(workspaceKey, workspaceToken)
          : Promise.resolve([]),
        availableViews.includes("ORDER_FEE_CROSS_CHECK")
          ? fetchWorkspaceSnapshots(workspaceKey, workspaceToken)
          : Promise.resolve([]),
        availableViews.includes("DAILY")
          ? fetchWorkspaceDailyRows(workspaceKey, workspaceToken)
          : Promise.resolve([]),
        availableViews.includes("MONTHLY")
          ? fetchWorkspaceMonthlyRows(workspaceKey, workspaceToken)
          : Promise.resolve([]),
        availableViews.includes("ORDER_DETAIL")
          ? fetchWorkspaceOrderRows(workspaceKey, workspaceToken)
          : Promise.resolve([]),
        availableViews.includes("FEE_DETAIL")
          ? fetchWorkspaceFeeRows(workspaceKey, workspaceToken)
          : Promise.resolve([]),
      ]);

    setIssues(issuesRes as IssueRow[]);
    setSnapshots(snapshotsRes as SnapshotRow[]);
    setDailyRows(dailyRes as DailyRow[]);
    setMonthlyRows(monthlyRes as MonthlyRow[]);
    setOrderRows(ordersRes as OrderRow[]);
    setFeeRows(feesRes as FeeRow[]);
  }

  async function refreshAnalysisSets() {
    if (!isAuthenticated) {
      setAnalysisSets([]);
      return;
    }

    setAnalysisSetsLoading(true);

    try {
      const data = await fetchAnalysisSets();
      setAnalysisSets(data);
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error));
    } finally {
      setAnalysisSetsLoading(false);
    }
  }

async function initializeWorkspace(forceCreate = false): Promise<WorkspaceSession> {
  setPageLoading(true);
  setErrorMessage(null);

  try {
    const savedSession = loadWorkspaceSession();

    const candidateSession = forceCreate
      ? null
      : workspaceSession || (isAuthenticated ? savedSession : null);

    if (candidateSession) {
      const workspaceRes = await fetchWorkspace(
        candidateSession.workspaceKey,
        candidateSession.workspaceToken
      );

      const shouldDropEmptyGuestAfterLogin =
        isAuthenticated &&
        workspaceRes.ownerType === "GUEST" &&
        (workspaceRes.files?.length ?? 0) === 0 &&
        workspaceRes.savedAnalysisSetId == null;

      if (!shouldDropEmptyGuestAfterLogin) {
        const nextSession: WorkspaceSession = {
          ...candidateSession,
          savedAnalysisSetId: workspaceRes.savedAnalysisSetId ?? null,
        };

        setWorkspaceSession(nextSession);
        if (isAuthenticated) saveWorkspaceSession(nextSession);

        setWorkspace(workspaceRes);
        setContext(workspaceRes.context ?? null);
        setCapability(workspaceRes.capability ?? null);

        await loadWorkspaceResultData(
          nextSession.workspaceKey,
          nextSession.workspaceToken,
          workspaceRes.capability
        );

        setHasUnsavedWorkspaceChanges(false);
        return nextSession;
      }

      clearWorkspaceSession();
      setWorkspaceSession(null);
    }

    clearWorkspaceSession();
    setWorkspaceSession(null);

    const created = await createWorkspace();

    if (!created.workspaceToken) {
      throw new Error("workspaceToken 이 응답에 없습니다.");
    }

    const session: WorkspaceSession = {
      workspaceKey: created.workspaceKey,
      workspaceToken: created.workspaceToken,
      savedAnalysisSetId: null,
    };

    const workspaceRes = await fetchWorkspace(
      session.workspaceKey,
      session.workspaceToken
    );

    const nextSession: WorkspaceSession = {
      ...session,
      savedAnalysisSetId: workspaceRes.savedAnalysisSetId ?? null,
    };

    setWorkspaceSession(nextSession);
    if (isAuthenticated) saveWorkspaceSession(nextSession);

    setWorkspace(workspaceRes);
    setContext(workspaceRes.context ?? null);
    setCapability(workspaceRes.capability ?? null);

    await loadWorkspaceResultData(
      nextSession.workspaceKey,
      nextSession.workspaceToken,
      workspaceRes.capability
    );

    setHasUnsavedWorkspaceChanges(false);
    return nextSession;
  } catch (error) {
    clearWorkspaceSession();
    setWorkspaceSession(null);
    setWorkspace(null);
    setCapability(null);
    setContext(null);
    setIssues([]);
    setSnapshots([]);
    setDailyRows([]);
    setMonthlyRows([]);
    setOrderRows([]);
    setFeeRows([]);
    setErrorMessage(getApiErrorMessage(error));
    throw error;
  } finally {
    setPageLoading(false);
  }
}

  async function refreshWorkspace(sessionArg?: WorkspaceSession) {
  const session = sessionArg ?? workspaceSession;
  if (!session) return;

  const workspaceRes = await fetchWorkspace(
    session.workspaceKey,
    session.workspaceToken
  );

  setWorkspace(workspaceRes);
  setContext(workspaceRes.context ?? null);
  setCapability(workspaceRes.capability ?? null);

  const nextSession: WorkspaceSession = {
    ...session,
    savedAnalysisSetId: workspaceRes.savedAnalysisSetId ?? null,
  };

  setWorkspaceSession(nextSession);

  if (isAuthenticated) {
    saveWorkspaceSession(nextSession);
  }

  await loadWorkspaceResultData(
    nextSession.workspaceKey,
    nextSession.workspaceToken,
    workspaceRes.capability
  );
}
  async function handleUploadFile(file: File, fileType: SettlementFileType) {
    if (!workspaceSession || isViewingSavedAnalysis) return;

    setErrorMessage(null);

    try {
      await uploadWorkspaceFile(
        workspaceSession.workspaceKey,
        workspaceSession.workspaceToken,
        file,
        fileType
      );
      await refreshWorkspace();
      setHasUnsavedWorkspaceChanges(true);
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error));
    }
  }

  async function handleRemoveFile(workspaceFileId: number) {
    if (!workspaceSession || isViewingSavedAnalysis) return;

    setRemovingFile(true);
    setErrorMessage(null);

    try {
      await removeWorkspaceFile(
        workspaceSession.workspaceKey,
        workspaceSession.workspaceToken,
        workspaceFileId
      );
      await refreshWorkspace();
      setHasUnsavedWorkspaceChanges(true);
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error));
    } finally {
      setRemovingFile(false);
    }
  }

  function handleChangeContext(next: UpdateAnalysisContextRequest) {
    setContext((prev) => ({
      ...(prev ?? (createDefaultContext() as AnalysisContextResponse)),
      ...next,
    }));
  }

  async function handleSaveContext() {
    if (!workspaceSession || isViewingSavedAnalysis) return;

    setContextSaving(true);
    setErrorMessage(null);

    try {
      const payload = normalizedContext;
      const updated = await updateWorkspaceContext(
        workspaceSession.workspaceKey,
        workspaceSession.workspaceToken,
        payload
      );

      setContext((updated ?? payload) as AnalysisContextResponse);

      await loadWorkspaceResultData(
        workspaceSession.workspaceKey,
        workspaceSession.workspaceToken,
        capability ?? undefined
      );

      setIsOptionEditorOpen(false);
      setHasUnsavedWorkspaceChanges(true);
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error));
    } finally {
      setContextSaving(false);
    }
  }

  async function handleSaveWorkspace() {
    if (!isAuthenticated) {
      openAuthModal();
      return;
    }

    if (!workspaceSession || isViewingSavedAnalysis) return;

    setSaveLoading(true);
    setErrorMessage(null);

    try {
      await saveWorkspace(
        workspaceSession.workspaceKey,
        workspaceSession.workspaceToken
      );
      await refreshWorkspace();
      await refreshAnalysisSets();
      setHasUnsavedWorkspaceChanges(false);
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error));
    } finally {
      setSaveLoading(false);
    }
  }

 // saved analysis를 workspace에 복원한 직후 상태는 "저장된 내용을 펼쳐놓은 작업본"으로 간주한다.
// 따라서 사용자가 추가 수정하기 전까지는 unsaved change가 없는 상태(false)다.
async function performRestoreAnalysisSet(
  analysisSetId: number,
  sessionArg?: WorkspaceSession
) {
  await restoreAnalysisSetToWorkspace(analysisSetId);
  setSelectedAnalysisSetId(analysisSetId);
  setIsViewingSavedAnalysis(false);
  setIsSavedListOpen(false);
  await refreshWorkspace(sessionArg);
  setHasUnsavedWorkspaceChanges(false);
}

  async function handleOpenAnalysisSet(analysisSetId: number) {
    setErrorMessage(null);

    // 수정: ACTIVE/SAVED 여부가 아니라 미저장 변경 여부로 모달 판단
    if (hasUnsavedWorkspaceChanges) {
      setPendingRestoreAnalysisSetId(analysisSetId);
      setIsRestoreConfirmOpen(true);
      return;
    }

    setPageLoading(true);
    try {
      await performRestoreAnalysisSet(analysisSetId);
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error));
    } finally {
      setPageLoading(false);
    }
  }

async function handleConfirmSaveAndRestore() {
  if (!pendingRestoreAnalysisSetId) return;

  if (!isAuthenticated) {
    openAuthModal();
    return;
  }

  if (!workspaceSession) {
    setErrorMessage("현재 워크스페이스가 없습니다.");
    return;
  }

  setRestoreAfterSaveLoading(true);
  setErrorMessage(null);

  try {
    await saveWorkspace(
      workspaceSession.workspaceKey,
      workspaceSession.workspaceToken
    );

    await refreshAnalysisSets();

    const newSession = await initializeWorkspace(true);
    await performRestoreAnalysisSet(pendingRestoreAnalysisSetId, newSession);

    setIsRestoreConfirmOpen(false);
    setPendingRestoreAnalysisSetId(null);
    setHasUnsavedWorkspaceChanges(false);
  } catch (error) {
    setErrorMessage(getApiErrorMessage(error));
  } finally {
    setRestoreAfterSaveLoading(false);
  }
}

  async function handleConfirmRestoreWithoutSave() {
    if (!pendingRestoreAnalysisSetId) return;

    setRestoreWithoutSaveLoading(true);
    setErrorMessage(null);

    try {
      await performRestoreAnalysisSet(pendingRestoreAnalysisSetId);

      setIsRestoreConfirmOpen(false);
      setPendingRestoreAnalysisSetId(null);
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error));
    } finally {
      setRestoreWithoutSaveLoading(false);
    }
  }

  function handleCancelRestore() {
    setIsRestoreConfirmOpen(false);
    setPendingRestoreAnalysisSetId(null);
  }

  async function handleBackToWorkspace() {
    setIsViewingSavedAnalysis(false);
    setSelectedAnalysisSetId(null);
    setIsSavedListOpen(false);
    await refreshWorkspace();
  }

  async function handleLogout() {
    try {
      await authApi.logout();
    } catch {
      // ignore
    } finally {
      setSkipAutoInitOnce(true);
      clearAuth();
      clearWorkspaceSession();
      setWorkspaceSession(null);
      setWorkspace(null);
      setCapability(null);
      setContext(null);
      setIssues([]);
      setSnapshots([]);
      setDailyRows([]);
      setMonthlyRows([]);
      setOrderRows([]);
      setFeeRows([]);
      setAnalysisSets([]);
      setSelectedAnalysisSetId(null);
      setIsViewingSavedAnalysis(false);
      setIsOptionEditorOpen(false);
      setIsWorkspaceFilesOpen(false);
      setIsSavedListOpen(false);
      setIsRestoreConfirmOpen(false);
      setPendingRestoreAnalysisSetId(null);
      setHasUnsavedWorkspaceChanges(false);
    }
  }

  async function handleResetWorkspace() {
    clearWorkspaceSession();
    setWorkspaceSession(null);
    setWorkspace(null);
    setCapability(null);
    setContext(null);
    setIssues([]);
    setSnapshots([]);
    setDailyRows([]);
    setMonthlyRows([]);
    setOrderRows([]);
    setFeeRows([]);
    setSelectedAnalysisSetId(null);
    setIsViewingSavedAnalysis(false);
    setIsWorkspaceFilesOpen(false);
    setIsSavedListOpen(false);
    setIsRestoreConfirmOpen(false);
    setPendingRestoreAnalysisSetId(null);
    setHasUnsavedWorkspaceChanges(false);
    await initializeWorkspace(true);
  }

  useEffect(() => {
    if (!isAuthInitialized) return;
    if (isAuthenticated && !accessToken) return;

    if (skipAutoInitOnce) {
      setSkipAutoInitOnce(false);
      return;
    }

    initializeWorkspace();
  }, [isAuthInitialized, isAuthenticated, accessToken, skipAutoInitOnce]);

  useEffect(() => {
    if (!isAuthInitialized) return;

    if (!isAuthenticated) {
      setAnalysisSets([]);
      setSelectedAnalysisSetId(null);
      setIsViewingSavedAnalysis(false);
      return;
    }

    refreshAnalysisSets();
  }, [isAuthInitialized, isAuthenticated]);

  const tabs = useMemo(
    () => buildTabs(capability?.availableViews ?? []),
    [capability]
  );

  useEffect(() => {
    if (!tabs.some((tab) => tab.key === activeTab)) {
      setActiveTab("OVERVIEW");
    }
  }, [tabs, activeTab]);

  function renderResultContent() {
    if (activeTab === "OVERVIEW" || activeTab === "ISSUES") {
      if (!issues.length) {
        return (
          <EmptyState
            title="이슈 데이터가 없습니다"
            description="파일을 업로드하고 분석 옵션을 적용하면 이슈 리포트가 이 영역에 표시됩니다."
          />
        );
      }
      return <IssuesTable rows={issues} />;
    }

    if (activeTab === "SNAPSHOTS") {
      if (!snapshots.length) {
        return (
          <EmptyState
            title="스냅샷 데이터가 없습니다"
            description="주문과 수수료 비교 결과가 생성되면 여기에서 확인할 수 있습니다."
          />
        );
      }
      return <SnapshotsTable rows={snapshots} />;
    }

    if (activeTab === "DAILY") {
      if (!dailyRows.length) {
        return (
          <EmptyState
            title="일별 정산 데이터가 없습니다"
            description="일별 정산 파일 업로드 후 결과가 생성되면 이 표에 나타납니다."
          />
        );
      }
      return <DailyTable rows={dailyRows} />;
    }

    if (activeTab === "MONTHLY") {
      if (!monthlyRows.length) {
        return (
          <EmptyState
            title="월별 정산 데이터가 없습니다"
            description="월별 비교 데이터가 준비되면 이곳에 표시됩니다."
          />
        );
      }
      return <MonthlyTable rows={monthlyRows} />;
    }

    if (activeTab === "ORDERS") {
      if (!orderRows.length) {
        return (
          <EmptyState
            title="건별 정산 데이터가 없습니다"
            description="주문 상세 검증 결과가 생성되면 이곳에 표시됩니다."
          />
        );
      }
      return <OrdersTable rows={orderRows} />;
    }

    if (activeTab === "FEES") {
      if (!feeRows.length) {
        return (
          <EmptyState
            title="수수료 상세 데이터가 없습니다"
            description="수수료 상세 비교 결과가 생성되면 이곳에 표시됩니다."
          />
        );
      }
      return <FeesTable rows={feeRows} />;
    }

    return (
      <EmptyState
        title="표시할 데이터가 없습니다"
        description="업로드 후 결과가 생성되면 이 영역에 검증 리포트가 나타납니다."
      />
    );
  }
   const basedAnalysisSetId =
    workspaceSession?.savedAnalysisSetId ?? selectedAnalysisSetId ?? null;

  const basedAnalysisSet = basedAnalysisSetId
    ? analysisSets.find((item) => item.id === basedAnalysisSetId)
    : null;

  const currentWorkspaceLabel = basedAnalysisSet
    ? `${basedAnalysisSet.id}번 저장본 기반`
    : "새 작업공간";

  const currentWorkspaceStatusLabel = hasUnsavedWorkspaceChanges
    ? "변경사항 있음"
    : "변경사항 없음";

  const currentWorkspaceStatusTone = hasUnsavedWorkspaceChanges
    ? "bg-amber-50 text-amber-700 border-amber-200"
    : "bg-emerald-50 text-emerald-700 border-emerald-200";

  const capabilityViews = capability?.availableViews ?? [];

  return (
    <div className="h-screen overflow-hidden bg-[#f8fafc] font-sans text-slate-900">
      <div className="mx-auto flex h-full max-w-[1680px] flex-col px-3 py-3 sm:px-4 sm:py-4 xl:px-5 xl:py-5">
        <header className="mb-3 flex shrink-0 flex-col gap-3 sm:mb-4 sm:flex-row sm:items-center sm:justify-between">
          <div className="flex items-center gap-4">
            <h1 className="text-3xl font-black tracking-tighter text-slate-900">
              Sellivu
            </h1>
            <span className="rounded-md bg-blue-600 px-2 py-0.5 text-[10px] font-bold uppercase tracking-wider text-white">
              Analysis Hub
            </span>
          </div>

          <div className="flex flex-wrap items-center gap-2 sm:gap-3">
            {workspace && (
              <div className="hidden rounded-full border border-slate-200 bg-white px-4 py-1.5 text-[11px] font-bold uppercase text-slate-500 shadow-sm sm:flex sm:items-center sm:gap-2">
                <div
                  className={`h-1.5 w-1.5 rounded-full ${
                    workspace.status === "ACTIVE"
                      ? "bg-emerald-500"
                      : "bg-amber-500"
                  }`}
                />
                {workspace.status}
              </div>
            )}

            {isAuthenticated && user ? (
              <>
                <div className="rounded-xl border border-slate-200 bg-white px-4 py-2 text-xs font-bold text-slate-700 shadow-sm">
                  {user.name}
                </div>
                <button
                  type="button"
                  onClick={handleLogout}
                  className="rounded-xl border border-slate-200 bg-white px-4 py-2 text-xs font-bold text-slate-700 transition hover:bg-slate-50"
                >
                  로그아웃
                </button>
              </>
            ) : (
              <button
                type="button"
                onClick={openAuthModal}
                className="rounded-xl border border-slate-200 bg-white px-4 py-2 text-xs font-bold text-slate-700 transition hover:bg-slate-50"
              >
                로그인
              </button>
            )}

            {workspace && (
              <button
                type="button"
                onClick={handleResetWorkspace}
                className="rounded-xl bg-slate-900 px-4 py-2 text-xs font-bold text-white transition-all hover:bg-rose-500 active:scale-95"
              >
                초기화
              </button>
            )}
          </div>
        </header>

        {errorMessage && (
          <div className="mb-4 flex shrink-0 items-center gap-3 rounded-2xl border border-rose-100 bg-rose-50/70 p-4 text-sm font-medium text-rose-600">
            <AlertIcon className="h-5 w-5" />
            {errorMessage}
          </div>
        )}

        <div className="min-h-0 flex-1">
          <div className="flex h-full min-h-0 flex-col gap-4 xl:flex-row">
            <aside className="flex min-h-0 w-full shrink-0 flex-col xl:h-full xl:w-[400px] 2xl:w-[430px]">
              <section className="flex min-h-0 flex-1 flex-col overflow-hidden rounded-[28px] border border-slate-200 bg-white shadow-sm shadow-slate-200/60">
                <div className="min-h-0 flex-1 overflow-y-auto p-3 custom-scrollbar sm:p-4">
                  <div className="flex min-h-full flex-col gap-3">
                    <div className="grid grid-cols-[1.05fr_1.1fr] gap-3">
                      <section className="rounded-[24px] border border-slate-200 bg-white p-3 shadow-sm">
                        <div className="mb-3">
                          <p className="text-[11px] font-bold uppercase tracking-[0.22em] text-slate-400">
                            파일 업로드
                          </p>
                        </div>

                        <div className="min-h-[420px]">
                          <SettlementUploadSection
                            disabled={
                              !isActiveWorkspace || isViewingSavedAnalysis
                            }
                            onUpload={handleUploadFile}
                          />
                        </div>
                      </section>

                      <section className="rounded-[24px] border border-slate-200 bg-white p-3 shadow-sm">
                        <div className="mb-3">
                          <p className="text-[11px] font-bold uppercase tracking-[0.22em] text-slate-400">
                            분석 옵션 설정
                          </p>
                        </div>

                        <div className="min-h-[420px]">
                          <SettlementContextSummaryCard
                            context={normalizedContext}
                            onEdit={() => setIsOptionEditorOpen(true)}
                          />
                        </div>
                      </section>
                    </div>

                    <div className="grid shrink-0 grid-cols-1 gap-3">
                      <SummaryActionCard
                        title="연결된 파일"
                        count={workspaceFiles.length}
                        description="연결된 파일을 확인하고 제거할 수 있습니다."
                        buttonLabel="목록 보기"
                        onClick={() => setIsWorkspaceFilesOpen(true)}
                        tone="blue"
                      />

                      <SummaryActionCard
                        title="저장된 분석"
                        count={analysisSets.length}
                        description="저장한 분석을 열고 복원할 수 있습니다."
                        buttonLabel="목록 보기"
                        onClick={() => setIsSavedListOpen(true)}
                        tone="slate"
                      />
                    </div>
                  </div>
                </div>

                             <div className="shrink-0 border-t border-slate-100 bg-white px-3 py-3 sm:px-4">
                  <div className="mb-3 rounded-2xl border border-slate-200 bg-slate-50/80 px-4 py-3">
                    <p className="text-[11px] font-bold uppercase tracking-[0.2em] text-slate-400">
                      Current Workspace
                    </p>

                    <p className="mt-2 text-sm font-bold text-slate-800">
                      현재 작업공간: {currentWorkspaceLabel}
                    </p>

                    <div className="mt-2 flex flex-wrap items-center gap-2">
                      <span
                        className={`rounded-full border px-2.5 py-1 text-[11px] font-bold ${currentWorkspaceStatusTone}`}
                      >
                        {currentWorkspaceStatusLabel}
                      </span>

                      {basedAnalysisSet?.name && (
                        <span className="text-[11px] font-medium text-slate-500">
                          {basedAnalysisSet.name}
                        </span>
                      )}
                    </div>
                  </div>

                  <button
                    type="button"
                    onClick={handleSaveWorkspace}
                    disabled={!canSaveWorkspace}
                    className="w-full rounded-xl bg-blue-600 px-4 py-2.5 text-sm font-bold text-white shadow-md shadow-blue-100 hover:bg-blue-700 disabled:opacity-30"
                  >
                    {saveLoading ? "처리 중..." : "현재 작업 저장하기"}
                  </button>
                </div>
              </section>
            </aside>

            <main className="flex min-h-0 min-w-0 flex-1">
              <section className="flex min-h-0 min-w-0 flex-1 flex-col">
                <div className="mb-3 flex shrink-0 flex-col gap-3 xl:flex-row xl:items-start xl:justify-between">
                  <div className="min-w-0">
                    <p className="text-xs font-bold uppercase tracking-[0.2em] text-slate-400">
                      Result View
                    </p>

                    <div className="mt-1 flex flex-wrap items-center gap-3">
                      <h2 className="text-2xl font-black tracking-tight text-slate-900 sm:text-3xl">
                        상세 리포트
                      </h2>

                      {capabilityViews.length > 0 && (
                        <div className="flex flex-wrap gap-2">
                          {capabilityViews.map((view) => (
                            <CapabilityBadge
                              key={view}
                              label={FileTypeLabel(view)}
                            />
                          ))}
                        </div>
                      )}
                    </div>
                  </div>

                  <div className="shrink-0 rounded-2xl border border-slate-200 bg-white p-1.5 shadow-sm">
                    <SettlementResultTabs
                      tabs={tabs}
                      activeTab={activeTab}
                      onChange={setActiveTab}
                    />
                  </div>
                </div>

                <div className="mb-3 grid shrink-0 grid-cols-1 gap-3 sm:grid-cols-2 2xl:grid-cols-4">
                  <KpiCard
                    label="발견된 이슈"
                    value={issues.length}
                    tone={issues.length > 0 ? "rose" : "blue"}
                    icon={<IssueIcon />}
                  />
                  <KpiCard
                    label="검증 완료"
                    value={snapshots.length}
                    tone="emerald"
                    icon={<CheckIcon />}
                  />
                  <KpiCard
                    label="정산 행"
                    value={dailyRows.length}
                    tone="blue"
                    icon={<SnapshotIcon />}
                  />
                  <KpiCard
                    label="수수료 상세"
                    value={feeRows.length}
                    tone="amber"
                    icon={<AlertIcon />}
                  />
                </div>

                <div className="min-h-0 flex-1 rounded-[24px] border border-slate-200 bg-white p-3 shadow-xl shadow-slate-200/60 sm:p-4 xl:rounded-[30px]">
                  <div className="flex h-full min-h-0 flex-col overflow-hidden rounded-[18px] border border-slate-100 bg-slate-50/40 sm:rounded-[24px]">
                    <div className="shrink-0 border-b border-slate-100 px-4 py-4 sm:px-5">
                      <p className="text-sm font-bold text-slate-700">
                        결과 나오는 페이지
                      </p>
                      <p className="mt-1 text-xs text-slate-400">
                        업로드 및 옵션 적용 결과가 여기에 표시됩니다.
                      </p>
                    </div>

                    <div className="min-h-0 flex-1 overflow-auto p-3 sm:p-4 custom-scrollbar">
                      {pageLoading ? (
                        <div className="flex h-full items-center justify-center">
                          <div className="h-10 w-10 animate-spin rounded-full border-4 border-blue-600 border-t-transparent" />
                        </div>
                      ) : (
                        renderResultContent()
                      )}
                    </div>
                  </div>
                </div>
              </section>
            </main>
          </div>
        </div>

        {isOptionEditorOpen && (
          <div className="fixed inset-0 z-50 flex justify-end bg-slate-900/30 backdrop-blur-[1px]">
            <div className="flex h-full w-full max-w-[92vw] flex-col border-l border-slate-200 bg-white shadow-2xl sm:w-[520px]">
              <div className="flex items-center justify-between border-b border-slate-100 px-4 py-4 sm:px-6 sm:py-5">
                <div>
                  <p className="text-xs font-bold uppercase tracking-[0.2em] text-slate-400">
                    Analysis Options
                  </p>
                  <h3 className="mt-1 text-xl font-black tracking-tight text-slate-900 sm:text-2xl">
                    분석 옵션 편집
                  </h3>
                </div>

                <button
                  type="button"
                  onClick={() => setIsOptionEditorOpen(false)}
                  className="rounded-xl border border-slate-200 px-3 py-2 text-sm font-bold text-slate-600 hover:bg-slate-50"
                >
                  닫기
                </button>
              </div>

              <div className="min-h-0 flex-1 overflow-y-auto px-4 py-4 sm:px-6 sm:py-5 custom-scrollbar">
                <SettlementContextForm
                  value={context}
                  highlight={capability?.requiresContextOptions ?? false}
                  onChange={handleChangeContext}
                />
              </div>

              <div className="flex flex-col gap-3 border-t border-slate-100 px-4 py-4 sm:flex-row sm:items-center sm:justify-between sm:px-6">
                <p className="text-xs text-slate-400">
                  변경한 옵션은 적용 버튼을 눌러야 반영됩니다.
                </p>

                <div className="flex gap-2">
                  <button
                    type="button"
                    onClick={() => setIsOptionEditorOpen(false)}
                    className="rounded-xl border border-slate-200 bg-white px-4 py-2 text-sm font-bold text-slate-700 hover:bg-slate-50"
                  >
                    취소
                  </button>
                  <button
                    type="button"
                    onClick={handleSaveContext}
                    disabled={
                      contextSaving || isViewingSavedAnalysis || !workspaceSession
                    }
                    className="rounded-xl bg-blue-600 px-4 py-2 text-sm font-bold text-white hover:bg-blue-700 disabled:opacity-50"
                  >
                    {contextSaving ? "적용 중" : "옵션 적용"}
                  </button>
                </div>
              </div>
            </div>
          </div>
        )}

        {isWorkspaceFilesOpen && (
          <div className="fixed inset-0 z-50 flex justify-end bg-slate-900/30 backdrop-blur-[1px]">
            <div className="flex h-full w-full max-w-[92vw] flex-col border-l border-slate-200 bg-white shadow-2xl sm:w-[560px]">
              <div className="flex items-center justify-between border-b border-slate-100 px-4 py-4 sm:px-6 sm:py-5">
                <div>
                  <p className="text-xs font-bold uppercase tracking-[0.2em] text-slate-400">
                    Connected Files
                  </p>
                  <h3 className="mt-1 text-xl font-black tracking-tight text-slate-900 sm:text-2xl">
                    연결된 파일
                  </h3>
                </div>

                <button
                  type="button"
                  onClick={() => setIsWorkspaceFilesOpen(false)}
                  className="rounded-xl border border-slate-200 px-3 py-2 text-sm font-bold text-slate-600 hover:bg-slate-50"
                >
                  닫기
                </button>
              </div>

              <div className="shrink-0 border-b border-slate-100 px-4 py-3 sm:px-6">
                <div className="rounded-xl bg-slate-50 px-3 py-2 text-[12px] text-slate-600">
                  현재 워크스페이스에 연결된 파일은{" "}
                  <span className="font-bold">{workspaceFiles.length}개</span>
                  입니다.
                </div>
              </div>

              <div className="min-h-0 flex-1 overflow-y-auto px-4 py-4 sm:px-6 sm:py-5 custom-scrollbar">
                <UploadedFileListCard
                  items={workspaceFiles}
                  removing={removingFile || isViewingSavedAnalysis}
                  onRemove={handleRemoveFile}
                />
              </div>
            </div>
          </div>
        )}

        {isSavedListOpen && (
          <div className="fixed inset-0 z-50 flex justify-end bg-slate-900/30 backdrop-blur-[1px]">
            <div className="flex h-full w-full max-w-[92vw] flex-col border-l border-slate-200 bg-white shadow-2xl sm:w-[560px]">
              <div className="flex items-center justify-between border-b border-slate-100 px-4 py-4 sm:px-6 sm:py-5">
                <div>
                  <p className="text-xs font-bold uppercase tracking-[0.2em] text-slate-400">
                    Saved Analyses
                  </p>
                  <h3 className="mt-1 text-xl font-black tracking-tight text-slate-900 sm:text-2xl">
                    저장된 분석
                  </h3>
                </div>

                <button
                  type="button"
                  onClick={() => setIsSavedListOpen(false)}
                  className="rounded-xl border border-slate-200 px-3 py-2 text-sm font-bold text-slate-600 hover:bg-slate-50"
                >
                  닫기
                </button>
              </div>

              <div className="shrink-0 border-b border-slate-100 px-4 py-3 sm:px-6">
                <div className="rounded-xl bg-slate-50 px-3 py-2 text-[12px] text-slate-600">
                  저장된 분석은{" "}
                  <span className="font-bold">{analysisSets.length}개</span>
                  입니다.
                </div>
              </div>

              <div className="min-h-0 flex-1 overflow-y-auto px-4 py-4 sm:px-6 sm:py-5 custom-scrollbar">
                {!isAuthenticated ? (
                  <div className="rounded-xl border border-dashed border-slate-200 bg-white px-4 py-5 text-center text-[13px] text-slate-500">
                    로그인 후 저장본을 확인할 수 있습니다.
                  </div>
                ) : analysisSetsLoading ? (
                  <div className="rounded-xl border border-slate-200 bg-white px-4 py-5 text-center text-[13px] text-slate-500">
                    저장본 불러오는 중...
                  </div>
                ) : analysisSets.length === 0 ? (
                  <div className="rounded-xl border border-dashed border-slate-200 bg-white px-4 py-5 text-center text-[13px] text-slate-500">
                    저장된 분석이 없습니다.
                  </div>
                ) : (
                  <div className="space-y-3">
                    {analysisSets.map((item) => (
                      <button
                        key={item.id}
                        type="button"
                        onClick={() => handleOpenAnalysisSet(item.id)}
                        className={`w-full rounded-2xl border px-4 py-3 text-left transition ${
                          selectedAnalysisSetId === item.id &&
                          isViewingSavedAnalysis
                            ? "border-blue-200 bg-blue-50"
                            : "border-slate-200 bg-white hover:bg-slate-50"
                        }`}
                      >
                        <div className="truncate text-[14px] font-bold text-slate-800">
                          {item.name}
                        </div>
                        <div className="mt-1 text-[12px] text-slate-500">
                          ID {item.id}
                        </div>
                      </button>
                    ))}
                  </div>
                )}
              </div>

              {isViewingSavedAnalysis && (
                <div className="shrink-0 border-t border-slate-100 px-4 py-4 sm:px-6">
                  <button
                    type="button"
                    onClick={handleBackToWorkspace}
                    className="w-full rounded-xl border border-slate-200 bg-white px-4 py-2.5 text-sm font-bold text-slate-700 hover:bg-slate-50"
                  >
                    현재 워크스페이스로 돌아가기
                  </button>
                </div>
              )}
            </div>
          </div>
        )}

        {isRestoreConfirmOpen && (
          <div className="fixed inset-0 z-[60] flex items-center justify-center bg-slate-900/40 px-4 backdrop-blur-[1px]">
            <div className="w-full max-w-md rounded-[24px] border border-slate-200 bg-white p-5 shadow-2xl">
              <p className="text-xs font-bold uppercase tracking-[0.2em] text-slate-400">
                Restore Confirmation
              </p>

              <h3 className="mt-2 text-xl font-black tracking-tight text-slate-900">
                저장본을 불러오시겠습니까?
              </h3>

              <p className="mt-3 text-sm leading-6 text-slate-600">
                현재 작업 중인 파일과 옵션은 선택한 저장본 내용으로
                교체됩니다. 계속하려면 현재 작업을 저장하거나, 저장하지 않고
                바로 불러오세요.
              </p>

              <div className="mt-5 flex flex-col gap-2">
                <button
                  type="button"
                  onClick={handleConfirmSaveAndRestore}
                  disabled={restoreAfterSaveLoading || restoreWithoutSaveLoading}
                  className="w-full rounded-xl bg-blue-600 px-4 py-3 text-sm font-bold text-white transition hover:bg-blue-700 disabled:opacity-50"
                >
                  {restoreAfterSaveLoading
                    ? "저장 후 불러오는 중..."
                    : "저장 후 불러오기"}
                </button>

                <button
                  type="button"
                  onClick={handleConfirmRestoreWithoutSave}
                  disabled={restoreAfterSaveLoading || restoreWithoutSaveLoading}
                  className="w-full rounded-xl border border-slate-200 bg-white px-4 py-3 text-sm font-bold text-slate-700 transition hover:bg-slate-50 disabled:opacity-50"
                >
                  {restoreWithoutSaveLoading
                    ? "불러오는 중..."
                    : "저장하지 않고 불러오기"}
                </button>

                <button
                  type="button"
                  onClick={handleCancelRestore}
                  disabled={restoreAfterSaveLoading || restoreWithoutSaveLoading}
                  className="w-full rounded-xl bg-slate-100 px-4 py-3 text-sm font-bold text-slate-600 transition hover:bg-slate-200 disabled:opacity-50"
                >
                  취소
                </button>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}