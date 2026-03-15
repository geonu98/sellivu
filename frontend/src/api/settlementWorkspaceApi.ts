import { http } from "./http";
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
  WorkspaceSaveResponse,
} from "../types/settlementAnalysis";

function workspaceHeaders(workspaceToken: string) {
  return {
    headers: {
      "X-Workspace-Token": workspaceToken,
    },
  };
}

export async function createWorkspace() {
  const { data } = await http.post<WorkspaceResponse>(
    "/api/settlement/workspaces"
  );
  return data;
}

export async function fetchWorkspace(
  workspaceKey: string,
  workspaceToken: string
) {
  const { data } = await http.get<WorkspaceResponse>(
    `/api/settlement/workspaces/${workspaceKey}`,
    workspaceHeaders(workspaceToken)
  );
  return data;
}

export async function fetchWorkspaceContext(
  workspaceKey: string,
  workspaceToken: string
) {
  const { data } = await http.get<AnalysisContextResponse>(
    `/api/settlement/workspaces/${workspaceKey}/context`,
    workspaceHeaders(workspaceToken)
  );
  return data;
}

export async function updateWorkspaceContext(
  workspaceKey: string,
  workspaceToken: string,
  body: UpdateAnalysisContextRequest
) {
  const { data } = await http.put<AnalysisContextResponse>(
    `/api/settlement/workspaces/${workspaceKey}/context`,
    body,
    workspaceHeaders(workspaceToken)
  );
  return data;
}

export async function fetchWorkspaceCapability(
  workspaceKey: string,
  workspaceToken: string
) {
  const { data } = await http.get<AnalysisCapabilityResponse>(
    `/api/settlement/workspaces/${workspaceKey}/capability`,
    workspaceHeaders(workspaceToken)
  );
  return data;
}

export async function uploadWorkspaceFile(
  workspaceKey: string,
  workspaceToken: string,
  file: File,
  fileType: SettlementFileType
) {
  const formData = new FormData();
  formData.append("file", file);
  formData.append("fileType", fileType);

  const { data } = await http.post(
    `/api/settlement/workspaces/${workspaceKey}/uploads`,
    formData,
    {
      headers: {
        "X-Workspace-Token": workspaceToken,
      },
    }
  );

  return data;
}

export async function fetchWorkspaceIssues(
  workspaceKey: string,
  workspaceToken: string
) {
  const { data } = await http.get<IssueRow[]>(
    `/api/settlement/workspaces/${workspaceKey}/issues`,
    workspaceHeaders(workspaceToken)
  );
  return data;
}

export async function fetchWorkspaceSnapshots(
  workspaceKey: string,
  workspaceToken: string
) {
  const { data } = await http.get<SnapshotRow[]>(
    `/api/settlement/workspaces/${workspaceKey}/snapshots`,
    workspaceHeaders(workspaceToken)
  );
  return data;
}

export async function fetchWorkspaceDailyRows(
  workspaceKey: string,
  workspaceToken: string
) {
  const { data } = await http.get<DailyRow[]>(
    `/api/settlement/workspaces/${workspaceKey}/daily`,
    workspaceHeaders(workspaceToken)
  );
  return data;
}

export async function fetchWorkspaceMonthlyRows(
  workspaceKey: string,
  workspaceToken: string
) {
  const { data } = await http.get<MonthlyRow[]>(
    `/api/settlement/workspaces/${workspaceKey}/monthly`,
    workspaceHeaders(workspaceToken)
  );
  return data;
}

export async function fetchWorkspaceOrderRows(
  workspaceKey: string,
  workspaceToken: string
) {
  const { data } = await http.get<OrderRow[]>(
    `/api/settlement/workspaces/${workspaceKey}/orders`,
    workspaceHeaders(workspaceToken)
  );
  return data;
}

export async function fetchWorkspaceFeeRows(
  workspaceKey: string,
  workspaceToken: string
) {
  const { data } = await http.get<FeeRow[]>(
    `/api/settlement/workspaces/${workspaceKey}/fees`,
    workspaceHeaders(workspaceToken)
  );
  return data;
}

export async function saveWorkspace(
  workspaceKey: string,
  workspaceToken: string
) {
  const { data } = await http.post<WorkspaceSaveResponse>(
    `/api/settlement/workspaces/${workspaceKey}/save`,
    {},
    workspaceHeaders(workspaceToken)
  );
  return data;
}


export async function removeWorkspaceFile(
  workspaceKey: string,
  workspaceToken: string,
  workspaceFileId: number
) {
  const { data } = await http.delete(
    `/api/settlement/workspaces/${workspaceKey}/files/${workspaceFileId}`,
    workspaceHeaders(workspaceToken)
  );
  return data;
}