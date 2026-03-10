import { useEffect, useMemo, useState } from "react";
import {
  createAnalysisSet,
  fetchAnalysisCapability,
  fetchAnalysisContext,
  fetchAnalysisSetItems,
  fetchAnalysisSets,
  fetchDailyRows,
  fetchFeeRows,
  fetchIssues,
  fetchMonthlyRows,
  fetchOrderRows,
  fetchSnapshots,
  linkUploadToAnalysisSet,
  rebuildAnalysisSet,
  updateAnalysisContext,
} from "../../api/settlementAnalysisApi";
import { uploadSettlementFile } from "../../api/settlementUploadApi";
import SettlementLeftPanel from "../../components/settlement/layout/SettlementLeftPanel";
import SettlementRightPanel from "../../components/settlement/layout/SettlementRightPanel";
import type {
  AnalysisCapabilityResponse,
  AnalysisContextResponse,
  AnalysisSetItemResponse,
  AnalysisSetResponse,
  DailyRow,
  FeeRow,
  IssueRow,
  MonthlyRow,
  OrderRow,
  SettlementFileType,
  SnapshotRow,
  UpdateAnalysisContextRequest,
} from "../../types/settlementAnalysis";
import type { UploadedFileItem } from "../../types/settlementUi";
import { buildTabs, type TabKey } from "../../utils/settlementTab";
import { getApiErrorMessage } from "../../utils/apiError";
import { useAuthStore } from "../../store/authStore";

export default function SettlementAnalysisPage() {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  const openAuthModal = useAuthStore((state) => state.openAuthModal);
  const user = useAuthStore((state) => state.user);

  const [analysisSets, setAnalysisSets] = useState<AnalysisSetResponse[]>([]);
  const [selectedAnalysisSetId, setSelectedAnalysisSetId] = useState<number | null>(null);

  const [items, setItems] = useState<AnalysisSetItemResponse[]>([]);
  const [capability, setCapability] = useState<AnalysisCapabilityResponse | null>(null);
  const [context, setContext] = useState<AnalysisContextResponse | null>(null);

  const [issues, setIssues] = useState<IssueRow[]>([]);
  const [snapshots, setSnapshots] = useState<SnapshotRow[]>([]);
  const [dailyRows, setDailyRows] = useState<DailyRow[]>([]);
  const [monthlyRows, setMonthlyRows] = useState<MonthlyRow[]>([]);
  const [orderRows, setOrderRows] = useState<OrderRow[]>([]);
  const [feeRows, setFeeRows] = useState<FeeRow[]>([]);
  const [uploadedFiles, setUploadedFiles] = useState<UploadedFileItem[]>([]);

  const [activeTab, setActiveTab] = useState<TabKey>("OVERVIEW");

  const [pageLoading, setPageLoading] = useState(false);
  const [contextSaving, setContextSaving] = useState(false);
  const [rebuildLoading, setRebuildLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const [showProgress, setShowProgress] = useState(false);
  const [progressStep, setProgressStep] = useState(0);

  async function animateProgress() {
    setShowProgress(true);
    setProgressStep(0);

    await new Promise((resolve) => setTimeout(resolve, 250));
    setProgressStep(1);

    await new Promise((resolve) => setTimeout(resolve, 300));
    setProgressStep(2);

    await new Promise((resolve) => setTimeout(resolve, 300));
    setProgressStep(3);
  }

  function resetAnalysisDetailState() {
    setItems([]);
    setCapability(null);
    setContext(null);
    setIssues([]);
    setSnapshots([]);
    setDailyRows([]);
    setMonthlyRows([]);
    setOrderRows([]);
    setFeeRows([]);
    setUploadedFiles([]);
    setActiveTab("OVERVIEW");
  }

  async function loadAnalysisSets() {
    const data = await fetchAnalysisSets();
    setAnalysisSets(Array.isArray(data) ? data : []);
  }

  async function loadBaseData(analysisSetId: number) {
    const [itemsRes, capabilityRes, contextRes] = await Promise.all([
      fetchAnalysisSetItems(analysisSetId),
      fetchAnalysisCapability(analysisSetId),
      fetchAnalysisContext(analysisSetId),
    ]);

    setItems(itemsRes);
    setCapability(capabilityRes);
    setContext(contextRes);

    return capabilityRes;
  }

  async function loadResultData(
    analysisSetId: number,
    capabilityRes?: AnalysisCapabilityResponse
  ) {
    const availableViews = capabilityRes?.availableViews ?? capability?.availableViews ?? [];

    const [issuesRes, snapshotsRes, dailyRes, monthlyRes, ordersRes, feesRes] =
      await Promise.all([
        availableViews.includes("ISSUES") ? fetchIssues(analysisSetId) : Promise.resolve([]),
        availableViews.includes("ORDER_FEE_CROSS_CHECK")
          ? fetchSnapshots(analysisSetId)
          : Promise.resolve([]),
        availableViews.includes("DAILY") ? fetchDailyRows(analysisSetId) : Promise.resolve([]),
        availableViews.includes("MONTHLY")
          ? fetchMonthlyRows(analysisSetId)
          : Promise.resolve([]),
        availableViews.includes("ORDER_DETAIL")
          ? fetchOrderRows(analysisSetId)
          : Promise.resolve([]),
        availableViews.includes("FEE_DETAIL")
          ? fetchFeeRows(analysisSetId)
          : Promise.resolve([]),
      ]);

    setIssues(issuesRes as IssueRow[]);
    setSnapshots(snapshotsRes as SnapshotRow[]);
    setDailyRows(dailyRes as DailyRow[]);
    setMonthlyRows(monthlyRes as MonthlyRow[]);
    setOrderRows(ordersRes as OrderRow[]);
    setFeeRows(feesRes as FeeRow[]);
  }

  async function handleCreateAnalysisSet(name: string) {
    if (!isAuthenticated) {
      openAuthModal();
      return;
    }

    try {
      setErrorMessage(null);

      const created = await createAnalysisSet({ name });
      await loadAnalysisSets();
      setSelectedAnalysisSetId(created.id);
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error));
    }
  }

  async function handleUploadFile(file: File, fileType: SettlementFileType) {
    if (!isAuthenticated) {
      openAuthModal();
      return;
    }

    if (!selectedAnalysisSetId) return;

    const tempId = `${Date.now()}-${Math.random()}`;

    setUploadedFiles((prev) => [
      {
        id: tempId,
        fileName: file.name,
        fileType,
        size: file.size,
        uploadedAt: new Date().toLocaleString("ko-KR"),
        status: "UPLOADING",
      },
      ...prev,
    ]);

    setErrorMessage(null);

    try {
      const uploaded = await uploadSettlementFile(file, fileType);
      await linkUploadToAnalysisSet(selectedAnalysisSetId, uploaded.uploadId);

      setUploadedFiles((prev) =>
        prev.map((item) =>
          item.id === tempId ? { ...item, status: "CONNECTED" } : item
        )
      );

      const capabilityRes = await loadBaseData(selectedAnalysisSetId);
      await loadResultData(selectedAnalysisSetId, capabilityRes);
    } catch (error) {
      setUploadedFiles((prev) =>
        prev.map((item) =>
          item.id === tempId ? { ...item, status: "FAILED" } : item
        )
      );

      setErrorMessage(getApiErrorMessage(error));
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
    if (!isAuthenticated) {
      openAuthModal();
      return;
    }

    if (!selectedAnalysisSetId || !context) return;

    setContextSaving(true);
    setErrorMessage(null);

    try {
      const updated = await updateAnalysisContext(selectedAnalysisSetId, {
        storeCouponUsage: context.storeCouponUsage,
        naverCouponUsage: context.naverCouponUsage,
        pointBenefitUsage: context.pointBenefitUsage,
        safeReturnCareUsage: context.safeReturnCareUsage,
        bizWalletOffsetUsage: context.bizWalletOffsetUsage,
        fastSettlementUsage: context.fastSettlementUsage,
        claimIncluded: context.claimIncluded,
      });

      setContext(updated);
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error));
    } finally {
      setContextSaving(false);
    }
  }

  async function handleRebuild() {
    if (!isAuthenticated) {
      openAuthModal();
      return;
    }

    if (!selectedAnalysisSetId) return;

    setRebuildLoading(true);
    setErrorMessage(null);

    try {
      await Promise.all([
        animateProgress(),
        (async () => {
          await rebuildAnalysisSet(selectedAnalysisSetId);
          const capabilityRes = await loadBaseData(selectedAnalysisSetId);
          await loadResultData(selectedAnalysisSetId, capabilityRes);
        })(),
      ]);
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error));
    } finally {
      setTimeout(() => {
        setShowProgress(false);
      }, 500);
      setRebuildLoading(false);
    }
  }

  useEffect(() => {
    if (!isAuthenticated) {
      setAnalysisSets([]);
      setSelectedAnalysisSetId(null);
      resetAnalysisDetailState();
      setErrorMessage(null);
      return;
    }

    (async () => {
      setPageLoading(true);
      setErrorMessage(null);

      try {
        await loadAnalysisSets();
      } catch (error) {
        setErrorMessage(getApiErrorMessage(error));
      } finally {
        setPageLoading(false);
      }
    })();
  }, [isAuthenticated]);

  useEffect(() => {
    if (selectedAnalysisSetId !== null) return;
    resetAnalysisDetailState();
  }, [selectedAnalysisSetId]);

  useEffect(() => {
    if (!isAuthenticated) return;
    if (!selectedAnalysisSetId) return;

    (async () => {
      setPageLoading(true);
      setErrorMessage(null);

      try {
        const capabilityRes = await loadBaseData(selectedAnalysisSetId);
        await loadResultData(selectedAnalysisSetId, capabilityRes);
      } catch (error) {
        setErrorMessage(getApiErrorMessage(error));
      } finally {
        setPageLoading(false);
      }
    })();
  }, [isAuthenticated, selectedAnalysisSetId]);

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
          analysis set 기준으로 파일을 연결하고, context 옵션을 반영해 정산 결과를 분석합니다.
        </p>
      </div>

      {!isAuthenticated && (
        <div className="rounded-xl border border-blue-200 bg-blue-50 p-4 text-sm text-blue-700">
          현재 게스트 모드입니다. 로그인하면 분석 세트 저장, 이전 작업 조회, 결과 재분석 기능을 사용할 수 있습니다.
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
            analysisSets={analysisSets}
            selectedAnalysisSetId={selectedAnalysisSetId}
            items={items}
            context={context}
            capability={capability}
            uploadedFiles={uploadedFiles}
            contextSaving={contextSaving}
            rebuildLoading={rebuildLoading}
            onCreateAnalysisSet={handleCreateAnalysisSet}
            onSelectAnalysisSet={setSelectedAnalysisSetId}
            onUploadFile={handleUploadFile}
            onChangeContext={handleChangeContext}
            onSaveContext={handleSaveContext}
            onRebuild={handleRebuild}
          />
        </aside>

        <main className="col-span-8">
          {!isAuthenticated ? (
            <div className="rounded-2xl border bg-white p-8 text-sm text-slate-500">
              현재 화면은 analysis set 저장형 구조라서, 로그인 후 분석 세트를 생성하면 업로드/연결/재분석 결과를 사용할 수 있습니다.
            </div>
          ) : !selectedAnalysisSetId ? (
            <div className="rounded-2xl border bg-white p-8 text-sm text-slate-500">
              분석 세트를 선택하거나 새로 생성하세요.
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
              showProgress={showProgress}
              progressStep={progressStep}
            />
          )}
        </main>
      </div>
    </div>
  );
}