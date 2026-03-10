import { http } from "./http";
import type {
  AnalysisCapabilityResponse,
  AnalysisContextResponse,
  AnalysisSetItemResponse,
  AnalysisSetResponse,
  CreateAnalysisSetRequest,
  DailyRow,
  FeeRow,
  IssueRow,
  MonthlyRow,
  OrderRow,
  RebuildAnalysisResponse,
  SnapshotRow,
  UpdateAnalysisContextRequest,
} from "../types/settlementAnalysis";

export async function createAnalysisSet(body: CreateAnalysisSetRequest) {
  const { data } = await http.post<AnalysisSetResponse>(
    "/api/settlement/analysis-sets",
    body
  );
  return data;
}

export async function fetchAnalysisSets() {
  const { data } = await http.get<AnalysisSetResponse[]>(
    "/api/settlement/analysis-sets"
  );
  return data;
}

export async function linkUploadToAnalysisSet(
  analysisSetId: number,
  uploadId: number
) {
  const { data } = await http.post(
    `/api/settlement/analysis-sets/${analysisSetId}/uploads/${uploadId}`
  );
  return data;
}

export async function fetchAnalysisSetItems(analysisSetId: number) {
  const { data } = await http.get<AnalysisSetItemResponse[]>(
    `/api/settlement/analysis-sets/${analysisSetId}/items`
  );
  return data;
}

export async function fetchAnalysisCapability(analysisSetId: number) {
  const { data } = await http.get<AnalysisCapabilityResponse>(
    `/api/settlement/analysis-capability/${analysisSetId}`
  );
  return data;
}

export async function fetchAnalysisContext(analysisSetId: number) {
  const { data } = await http.get<AnalysisContextResponse>(
    `/api/settlement/analysis-sets/${analysisSetId}/context`
  );
  return data;
}

export async function updateAnalysisContext(
  analysisSetId: number,
  body: UpdateAnalysisContextRequest
) {
  const { data } = await http.put<AnalysisContextResponse>(
    `/api/settlement/analysis-sets/${analysisSetId}/context`,
    body
  );
  return data;
}

export async function rebuildAnalysisSet(analysisSetId: number) {
  const { data } = await http.post<RebuildAnalysisResponse>(
    `/api/settlement/analysis-sets/${analysisSetId}/rebuild`
  );
  return data;
}

export async function fetchSnapshots(analysisSetId: number) {
  const { data } = await http.get<SnapshotRow[]>(
    `/api/settlement/analysis-sets/${analysisSetId}/snapshots`
  );
  return data;
}

export async function fetchIssues(analysisSetId: number) {
  const { data } = await http.get<IssueRow[]>(
    `/api/settlement/analysis-sets/${analysisSetId}/issues`
  );
  return data;
}

export async function fetchDailyRows(analysisSetId: number) {
  const { data } = await http.get<DailyRow[]>(
    `/api/settlement/analysis-sets/${analysisSetId}/daily`
  );
  return data;
}

export async function fetchMonthlyRows(analysisSetId: number) {
  const { data } = await http.get<MonthlyRow[]>(
    `/api/settlement/analysis-sets/${analysisSetId}/monthly`
  );
  return data;
}

export async function fetchOrderRows(analysisSetId: number) {
  const { data } = await http.get<OrderRow[]>(
    `/api/settlement/analysis-sets/${analysisSetId}/orders`
  );
  return data;
}

export async function fetchFeeRows(analysisSetId: number) {
  const { data } = await http.get<FeeRow[]>(
    `/api/settlement/analysis-sets/${analysisSetId}/fees`
  );
  return data;
}