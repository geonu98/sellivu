export type TabKey =
  | "OVERVIEW"
  | "ISSUES"
  | "SNAPSHOTS"
  | "DAILY"
  | "MONTHLY"
  | "ORDERS"
  | "FEES";

export function buildTabs(availableViews: string[]) {
  const tabs: { key: TabKey; label: string }[] = [
    { key: "OVERVIEW", label: "개요" },
  ];

  if (availableViews.includes("ISSUES")) {
    tabs.push({ key: "ISSUES", label: "이슈" });
  }

  if (availableViews.includes("ORDER_FEE_CROSS_CHECK")) {
    tabs.push({ key: "SNAPSHOTS", label: "스냅샷" });
  }

  if (availableViews.includes("DAILY")) {
    tabs.push({ key: "DAILY", label: "일별" });
  }

  if (availableViews.includes("MONTHLY")) {
    tabs.push({ key: "MONTHLY", label: "월별" });
  }

  if (availableViews.includes("ORDER_DETAIL")) {
    tabs.push({ key: "ORDERS", label: "건별" });
  }

  if (availableViews.includes("FEE_DETAIL")) {
    tabs.push({ key: "FEES", label: "수수료" });
  }

  return tabs;
}