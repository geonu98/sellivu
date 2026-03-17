import { http } from "./http";
import type {
  AnalysisSetResponse,
  AnalysisSetItemResponse,
  AnalysisCapabilityResponse,
  IssueRow,
  SnapshotRow,
  DailyRow,
  MonthlyRow,
  OrderRow,
  FeeRow,
} from "../types/settlementAnalysis";

export async function fetchAnalysisSets() {
  const { data } = await http.get<AnalysisSetResponse[]>(
    "/api/settlement/analysis-sets/my"
  );
  return data;
}

export async function fetchAnalysisSetDetail(analysisSetId: number) {
  const { data } = await http.get<AnalysisSetResponse>(
    `/api/settlement/analysis-sets/my/${analysisSetId}`
  );
  return data;
}

export async function fetchAnalysisSetItems(analysisSetId: number) {
  const { data } = await http.get<AnalysisSetItemResponse[]>(
    `/api/settlement/analysis-sets/my/${analysisSetId}/items`
  );
  return data;
}

export async function fetchAnalysisSetCapability(analysisSetId: number) {
  const { data } = await http.get<AnalysisCapabilityResponse>(
    `/api/settlement/analysis-capability/${analysisSetId}`
  );
  return data;
}

export async function fetchAnalysisSetIssues(analysisSetId: number) {
  const { data } = await http.get<IssueRow[]>(
    `/api/settlement/analysis-sets/my/${analysisSetId}/issues`
  );
  return data;
}

export async function fetchAnalysisSetSnapshots(analysisSetId: number) {
  const { data } = await http.get<SnapshotRow[]>(
    `/api/settlement/analysis-sets/my/${analysisSetId}/snapshots`
  );
  return data;
}

export async function fetchAnalysisSetDailyRows(analysisSetId: number) {
  const { data } = await http.get<DailyRow[]>(
    `/api/settlement/analysis-sets/my/${analysisSetId}/daily`
  );
  return data;
}

export async function fetchAnalysisSetMonthlyRows(analysisSetId: number) {
  const { data } = await http.get<MonthlyRow[]>(
    `/api/settlement/analysis-sets/my/${analysisSetId}/monthly`
  );
  return data;
}

export async function fetchAnalysisSetOrderRows(analysisSetId: number) {
  const { data } = await http.get<OrderRow[]>(
    `/api/settlement/analysis-sets/my/${analysisSetId}/orders`
  );
  return data;
}

export async function fetchAnalysisSetFeeRows(analysisSetId: number) {
  const { data } = await http.get<FeeRow[]>(
    `/api/settlement/analysis-sets/my/${analysisSetId}/fees`
  );
  return data;
}