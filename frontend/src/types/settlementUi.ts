export type UploadedFileItem = {
  id: string;
  fileName: string;
  fileType: "DAILY_SETTLEMENT" | "ORDER_SETTLEMENT" | "FEE_DETAIL";
  size: number;
  uploadedAt: string;
  status: "UPLOADING" | "CONNECTED" | "FAILED";
};