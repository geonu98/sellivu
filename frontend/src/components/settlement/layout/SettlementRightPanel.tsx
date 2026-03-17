import type {
  AnalysisCapabilityResponse,
  DailyRow,
  FeeRow,
  IssueRow,
  MonthlyRow,
  OrderRow,
  SnapshotRow,
} from "../../../types/settlementAnalysis";
import type { TabKey } from "../../../utils/settlementTab";
import CapabilitySummaryCard from "../capability/CapabilitySummaryCard";
import ResultSummaryCards from "../capability/ResultSummaryCards";
import SettlementResultTabs from "../tabs/SettlementResultTabs";
import IssuesTable from "../tabs/IssuesTable";
import SnapshotsTable from "../tabs/SnapshotsTable";
import DailyTable from "../tabs/DailyTable";
import MonthlyTable from "../tabs/MonthlyTable";
import OrdersTable from "../tabs/OrdersTable";
import FeesTable from "../tabs/FeesTable";

type Props = {
  capability: AnalysisCapabilityResponse | null;
  tabs: { key: TabKey; label: string }[];
  activeTab: TabKey;
  onChangeTab: (tab: TabKey) => void;
  issues: IssueRow[];
  snapshots: SnapshotRow[];
  dailyRows: DailyRow[];
  monthlyRows: MonthlyRow[];
  orderRows: OrderRow[];
  feeRows: FeeRow[];
};

export default function SettlementRightPanel({
  capability,
  tabs,
  activeTab,
  onChangeTab,
  issues,
  snapshots,
  dailyRows,
  monthlyRows,
  orderRows,
  feeRows,
}: Props) {
  return (
    <section className="flex h-full flex-col rounded-2xl border border-slate-200 bg-white shadow-sm">
      <div className="border-b border-slate-100 px-6 py-5">
        <p className="text-xs font-semibold uppercase tracking-[0.12em] text-slate-400">
          Result
        </p>
        <h2 className="mt-1 text-lg font-semibold text-slate-900">
          정산 분석 결과
        </h2>
        <p className="mt-1 text-sm text-slate-500">
          이슈, 스냅샷, 정산 데이터 비교 결과를 한 화면에서 확인합니다.
        </p>
      </div>

      <div className="flex-1 space-y-5 overflow-y-auto px-6 py-5">
        <div className="grid gap-4 xl:grid-cols-[1.15fr_0.85fr]">
          <div className="rounded-2xl border border-slate-200 bg-slate-50 p-4">
            <CapabilitySummaryCard capability={capability} />
          </div>
          <div className="rounded-2xl border border-slate-200 bg-slate-50 p-4">
            <ResultSummaryCards issues={issues} snapshots={snapshots} />
          </div>
        </div>

        <div className="rounded-2xl border border-slate-200 bg-white">
          <div className="border-b border-slate-100 px-4 py-3">
            <SettlementResultTabs
              tabs={tabs}
              activeTab={activeTab}
              onChange={onChangeTab}
            />
          </div>

          <div className="p-4">
            {activeTab === "OVERVIEW" && <IssuesTable rows={issues} />}
            {activeTab === "ISSUES" && <IssuesTable rows={issues} />}
            {activeTab === "SNAPSHOTS" && <SnapshotsTable rows={snapshots} />}
            {activeTab === "DAILY" && <DailyTable rows={dailyRows} />}
            {activeTab === "MONTHLY" && <MonthlyTable rows={monthlyRows} />}
            {activeTab === "ORDERS" && <OrdersTable rows={orderRows} />}
            {activeTab === "FEES" && <FeesTable rows={feeRows} />}
          </div>
        </div>
      </div>
    </section>
  );
}