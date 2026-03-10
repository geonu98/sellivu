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
import AnalysisProgressPanel from "../capability/AnalysisProgressPanel";
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
  showProgress: boolean;
  progressStep: number;
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
  showProgress,
  progressStep,
}: Props) {
  return (
    <div className="space-y-4">
      <AnalysisProgressPanel visible={showProgress} step={progressStep} />
      <CapabilitySummaryCard capability={capability} />
      <ResultSummaryCards issues={issues} snapshots={snapshots} />
      <SettlementResultTabs
        tabs={tabs}
        activeTab={activeTab}
        onChange={onChangeTab}
      />

      {activeTab === "OVERVIEW" && <IssuesTable rows={issues} />}
      {activeTab === "ISSUES" && <IssuesTable rows={issues} />}
      {activeTab === "SNAPSHOTS" && <SnapshotsTable rows={snapshots} />}
      {activeTab === "DAILY" && <DailyTable rows={dailyRows} />}
      {activeTab === "MONTHLY" && <MonthlyTable rows={monthlyRows} />}
      {activeTab === "ORDERS" && <OrdersTable rows={orderRows} />}
      {activeTab === "FEES" && <FeesTable rows={feeRows} />}
    </div>
  );
}