import type { SettlementFileType } from "./settlementAnalysis";

export type UploadedFileStatus = "UPLOADING" | "CONNECTED" | "FAILED";

export type UploadedFileItem = {
  id: string;
  fileName: string;
  fileType: SettlementFileType;
  size: number;
  uploadedAt: string;
  status: UploadedFileStatus;
};