import { http } from "./http";
import type {
  SettlementFileType,
  SettlementUploadResponse,
} from "../types/settlementAnalysis";

export async function uploadSettlementFile(
  file: File,
  fileType: SettlementFileType
) {
  const formData = new FormData();
  formData.append("file", file);
  formData.append("fileType", fileType);

  const { data } = await http.post<SettlementUploadResponse>(
    "/api/settlement/uploads",
    formData,
    {
      headers: {
        "Content-Type": "multipart/form-data",
      },
    }
  );

  return data;
}