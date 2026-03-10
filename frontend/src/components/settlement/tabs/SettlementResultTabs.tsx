import type { TabKey } from "../../../utils/settlementTab";

type TabItem = {
  key: TabKey;
  label: string;
};

type Props = {
  tabs: TabItem[];
  activeTab: TabKey;
  onChange: (tab: TabKey) => void;
};

export default function SettlementResultTabs({
  tabs,
  activeTab,
  onChange,
}: Props) {
  return (
    <div className="flex flex-wrap gap-2">
      {tabs.map((tab) => (
        <button
          key={tab.key}
          onClick={() => onChange(tab.key)}
          className={`rounded-full px-4 py-2 text-sm ${
            activeTab === tab.key
              ? "bg-black text-white"
              : "bg-slate-100 text-slate-700"
          }`}
        >
          {tab.label}
        </button>
      ))}
    </div>
  );
}