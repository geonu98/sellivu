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
import CapabilitySummaryCard from "../../../components/settlement/capability/CapabilitySummaryCard";
import ResultSummaryCards from "../../../components/settlement/capability/ResultSummaryCards";
import SettlementResultTabs from "../../../components/settlement/tabs/SettlementResultTabs";
import IssuesTable from "../../../components/settlement/tabs/IssuesTable";
import SnapshotsTable from "../../../components/settlement/tabs/SnapshotsTable";
import DailyTable from "../../../components/settlement/tabs/DailyTable";
import MonthlyTable from "../../../components/settlement/tabs/MonthlyTable";
import OrdersTable from "../../../components/settlement/tabs/OrdersTable";
import FeesTable from "../../../components/settlement/tabs/FeesTable";

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
    <div className="space-y-4">
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