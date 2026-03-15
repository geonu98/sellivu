import { useEffect, useMemo, useState } from "react";
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
import SettlementLeftPanel from "../../components/settlement/layout/SettlementLeftPanel";
import SettlementRightPanel from "../../components/settlement/layout/SettlementRightPanel";
import type {
  AnalysisCapabilityResponse,
  AnalysisContextResponse,
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

export default function SettlementAnalysisPage() {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  const openAuthModal = useAuthStore((state) => state.openAuthModal);
  const user = useAuthStore((state) => state.user);

  const [workspaceSession, setWorkspaceSession] = useState<WorkspaceSession | null>(null);
  const [workspace, setWorkspace] = useState<WorkspaceResponse | null>(null);

  const [capability, setCapability] = useState<AnalysisCapabilityResponse | null>(null);
  const [context, setContext] = useState<AnalysisContextResponse | null>(null);

  const [issues, setIssues] = useState<IssueRow[]>([]);
  const [snapshots, setSnapshots] = useState<SnapshotRow[]>([]);
  const [dailyRows, setDailyRows] = useState<DailyRow[]>([]);
  const [monthlyRows, setMonthlyRows] = useState<MonthlyRow[]>([]);
  const [orderRows, setOrderRows] = useState<OrderRow[]>([]);
  const [feeRows, setFeeRows] = useState<FeeRow[]>([]);

  const [activeTab, setActiveTab] = useState<TabKey>("OVERVIEW");

  const [pageLoading, setPageLoading] = useState(false);
  const [contextSaving, setContextSaving] = useState(false);
  const [saveLoading, setSaveLoading] = useState(false);
  const [removingFile, setRemovingFile] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  function resetAnalysisDetailState() {
    setCapability(null);
    setContext(null);
    setIssues([]);
    setSnapshots([]);
    setDailyRows([]);
    setMonthlyRows([]);
    setOrderRows([]);
    setFeeRows([]);
    setActiveTab("OVERVIEW");
  }

  async function loadWorkspaceResultData(
    workspaceKey: string,
    workspaceToken: string,
    capabilityRes?: AnalysisCapabilityResponse
  ) {
    const availableViews = capabilityRes?.availableViews ?? capability?.availableViews ?? [];

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

  async function initializeWorkspace() {
    setPageLoading(true);
    setErrorMessage(null);

    try {
      const savedSession = loadWorkspaceSession();

      if (savedSession) {
        const workspaceRes = await fetchWorkspace(
          savedSession.workspaceKey,
          savedSession.workspaceToken
        );

        const nextSession: WorkspaceSession = {
          ...savedSession,
          savedAnalysisSetId: workspaceRes.savedAnalysisSetId ?? null,
        };

        setWorkspaceSession(nextSession);
        saveWorkspaceSession(nextSession);
        setWorkspace(workspaceRes);
        setContext(workspaceRes.context ?? null);
        setCapability(workspaceRes.capability ?? null);

        await loadWorkspaceResultData(
          savedSession.workspaceKey,
          savedSession.workspaceToken,
          workspaceRes.capability
        );
        return;
      }

      const created = await createWorkspace();

      if (!created.workspaceToken) {
        throw new Error("workspaceToken 이 응답에 없습니다.");
      }

      const session: WorkspaceSession = {
        workspaceKey: created.workspaceKey,
        workspaceToken: created.workspaceToken,
        savedAnalysisSetId: null,
      };

      saveWorkspaceSession(session);
      setWorkspaceSession(session);

      const workspaceRes = await fetchWorkspace(
        session.workspaceKey,
        session.workspaceToken
      );

      const nextSession: WorkspaceSession = {
        ...session,
        savedAnalysisSetId: workspaceRes.savedAnalysisSetId ?? null,
      };

      setWorkspaceSession(nextSession);
      saveWorkspaceSession(nextSession);
      setWorkspace(workspaceRes);
      setContext(workspaceRes.context ?? null);
      setCapability(workspaceRes.capability ?? null);

      await loadWorkspaceResultData(
        nextSession.workspaceKey,
        nextSession.workspaceToken,
        workspaceRes.capability
      );
    } catch (error) {
      clearWorkspaceSession();
      setWorkspaceSession(null);
      setWorkspace(null);
      resetAnalysisDetailState();
      setErrorMessage(getApiErrorMessage(error));
    } finally {
      setPageLoading(false);
    }
  }

  async function refreshWorkspace() {
    if (!workspaceSession) return;

    const workspaceRes = await fetchWorkspace(
      workspaceSession.workspaceKey,
      workspaceSession.workspaceToken
    );

    setWorkspace(workspaceRes);
    setContext(workspaceRes.context ?? null);
    setCapability(workspaceRes.capability ?? null);

    const nextSession: WorkspaceSession = {
      ...workspaceSession,
      savedAnalysisSetId: workspaceRes.savedAnalysisSetId ?? null,
    };

    setWorkspaceSession(nextSession);
    saveWorkspaceSession(nextSession);

    await loadWorkspaceResultData(
      workspaceSession.workspaceKey,
      workspaceSession.workspaceToken,
      workspaceRes.capability
    );
  }

  async function handleUploadFile(file: File, fileType: SettlementFileType) {
    if (!workspaceSession) return;

    setErrorMessage(null);

    try {
      await uploadWorkspaceFile(
        workspaceSession.workspaceKey,
        workspaceSession.workspaceToken,
        file,
        fileType
      );

      await refreshWorkspace();
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error));
    }
  }

  async function handleRemoveFile(workspaceFileId: number) {
    if (!workspaceSession) return;

    setRemovingFile(true);
    setErrorMessage(null);

    try {
      await removeWorkspaceFile(
        workspaceSession.workspaceKey,
        workspaceSession.workspaceToken,
        workspaceFileId
      );
      await refreshWorkspace();
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error));
    } finally {
      setRemovingFile(false);
    }
  }

  function handleChangeContext(next: UpdateAnalysisContextRequest) {
    setContext((prev) => {
      if (!prev) return prev;
      return {
        ...prev,
        ...next,
      };
    });
  }

  async function handleSaveContext() {
    if (!workspaceSession || !context) return;

    setContextSaving(true);
    setErrorMessage(null);

    try {
      const updated = await updateWorkspaceContext(
        workspaceSession.workspaceKey,
        workspaceSession.workspaceToken,
        {
          storeCouponUsage: context.storeCouponUsage,
          naverCouponUsage: context.naverCouponUsage,
          pointBenefitUsage: context.pointBenefitUsage,
          safeReturnCareUsage: context.safeReturnCareUsage,
          bizWalletOffsetUsage: context.bizWalletOffsetUsage,
          fastSettlementUsage: context.fastSettlementUsage,
          claimIncluded: context.claimIncluded,
        }
      );

      setContext(updated);

      await loadWorkspaceResultData(
        workspaceSession.workspaceKey,
        workspaceSession.workspaceToken,
        capability ?? undefined
      );
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

    if (!workspaceSession) return;

    setSaveLoading(true);
    setErrorMessage(null);

    try {
      await saveWorkspace(
        workspaceSession.workspaceKey,
        workspaceSession.workspaceToken
      );
      await refreshWorkspace();
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error));
    } finally {
      setSaveLoading(false);
    }
  }

  async function handleResetWorkspace() {
    clearWorkspaceSession();
    setWorkspaceSession(null);
    setWorkspace(null);
    resetAnalysisDetailState();
    await initializeWorkspace();
  }

  useEffect(() => {
    initializeWorkspace();
  }, []);

  const tabs = useMemo(
    () => buildTabs(capability?.availableViews ?? []),
    [capability]
  );

  useEffect(() => {
    if (!tabs.some((tab) => tab.key === activeTab)) {
      setActiveTab("OVERVIEW");
    }
  }, [tabs, activeTab]);

  return (
    <div className="space-y-4 p-6">
      <div>
        <h1 className="text-2xl font-bold">정산 분석</h1>
        <p className="mt-1 text-sm text-slate-500">
          workspace 기준으로 파일을 업로드하고, context 옵션을 반영해 정산 결과를 분석합니다.
        </p>
      </div>

      {!isAuthenticated && (
        <div className="rounded-xl border border-blue-200 bg-blue-50 p-4 text-sm text-blue-700">
          현재 게스트 모드입니다. 지금 바로 업로드/분석은 가능하고, 로그인하면 저장 후 이전 작업 조회까지 가능합니다.
          <button
            className="ml-3 rounded-lg bg-black px-3 py-2 text-xs font-medium text-white"
            onClick={openAuthModal}
          >
            로그인 / 회원가입
          </button>
        </div>
      )}

      {isAuthenticated && user && (
        <div className="rounded-xl border border-slate-200 bg-white p-4 text-sm text-slate-700">
          <span className="font-medium">{user.name}</span>님으로 로그인되어 있습니다.
        </div>
      )}

      {workspace && (
        <div className="rounded-xl border border-slate-200 bg-white p-4 text-sm text-slate-700">
          <div className="flex flex-wrap items-center gap-3">
            <span>
              상태: <span className="font-medium">{workspace.status}</span>
            </span>
            <span>
              만료일:{" "}
              <span className="font-medium">
                {new Date(workspace.expiresAt).toLocaleString("ko-KR")}
              </span>
            </span>
            {workspace.savedAnalysisSetId && (
              <span>
                저장된 분석 ID:{" "}
                <span className="font-medium">{workspace.savedAnalysisSetId}</span>
              </span>
            )}
            <button
              type="button"
              onClick={handleResetWorkspace}
              className="rounded-lg border px-3 py-2 text-xs"
            >
              새 워크스페이스 시작
            </button>
          </div>
        </div>
      )}

      {errorMessage && (
        <div className="rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-700">
          {errorMessage}
        </div>
      )}

      {pageLoading && (
        <div className="rounded-xl border bg-white p-4 text-sm text-slate-500">
          데이터를 불러오는 중...
        </div>
      )}

      <div className="grid grid-cols-12 gap-6">
        <aside className="col-span-4">
          <SettlementLeftPanel
            context={context}
            capability={capability}
            workspaceFiles={(workspace?.files ?? []).filter((file) => file.active)}
            contextSaving={contextSaving}
            saveLoading={saveLoading}
            removingFile={removingFile}
            isAuthenticated={isAuthenticated}
            workspaceStatus={workspace?.status ?? null}
            onUploadFile={handleUploadFile}
            onRemoveFile={handleRemoveFile}
            onChangeContext={handleChangeContext}
            onSaveContext={handleSaveContext}
            onSaveWorkspace={handleSaveWorkspace}
          />
        </aside>

        <main className="col-span-8">
          {!workspaceSession ? (
            <div className="rounded-2xl border bg-white p-8 text-sm text-slate-500">
              워크스페이스를 준비하는 중입니다.
            </div>
          ) : (
            <SettlementRightPanel
              capability={capability}
              tabs={tabs}
              activeTab={activeTab}
              onChangeTab={setActiveTab}
              issues={issues}
              snapshots={snapshots}
              dailyRows={dailyRows}
              monthlyRows={monthlyRows}
              orderRows={orderRows}
              feeRows={feeRows}
            />
          )}
        </main>
      </div>
    </div>
  );
}