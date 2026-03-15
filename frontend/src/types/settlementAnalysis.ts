export type AnalysisOptionValue = "YES" | "NO" | "UNKNOWN";

export type SettlementFileType =
  | "DAILY_SETTLEMENT"
  | "ORDER_SETTLEMENT"
  | "FEE_DETAIL";

export type AnalysisSetResponse = {
  id: number;
  name: string;
  createdAt: string;
};

export type CreateAnalysisSetRequest = {
  name: string;
};

export type AnalysisSetItemResponse = {
  id: number;
  uploadId: number;
  fileType: SettlementFileType;
  originalFileName: string;
  linkedAt: string;
};

export type AnalysisCapabilityResponse = {
  uploadedFileTypes: SettlementFileType[];
  availableViews: string[];
  missingFileTypes: SettlementFileType[];
  gaps: string[];
  requiresContextOptions: boolean;
};

export type AnalysisContextResponse = {
  analysisSetId?: number | null;
  workspaceId?: number | null;
  storeCouponUsage: AnalysisOptionValue;
  naverCouponUsage: AnalysisOptionValue;
  pointBenefitUsage: AnalysisOptionValue;
  safeReturnCareUsage: AnalysisOptionValue;
  bizWalletOffsetUsage: AnalysisOptionValue;
  fastSettlementUsage: AnalysisOptionValue;
  claimIncluded: AnalysisOptionValue;
  updatedAt: string;
};

export type UpdateAnalysisContextRequest = {
  storeCouponUsage: AnalysisOptionValue;
  naverCouponUsage: AnalysisOptionValue;
  pointBenefitUsage: AnalysisOptionValue;
  safeReturnCareUsage: AnalysisOptionValue;
  bizWalletOffsetUsage: AnalysisOptionValue;
  fastSettlementUsage: AnalysisOptionValue;
  claimIncluded: AnalysisOptionValue;
};

export type SettlementUploadResponse = {
  uploadId: number;
  fileName: string;
  fileType: SettlementFileType;
  uploadedAt: string;
};

export type RebuildAnalysisResponse = {
  message: string;
  analysisSetId?: number;
  rebuiltUploadCount?: number;
  deletedSnapshotCount?: number;
};

export type IssueRow = {
  id: number | null;
  sourceType: string;
  snapshotId: number | null;
  issueType: string;
  orderNo: string | null;
  productOrderNo: string | null;
  joinKey: string | null;
  message: string;
  resolved: boolean;
  severity: string | null;
  judgementStatus: string | null;
  explanationCode: string | null;
  needsUserInput: boolean;
  possibleReasonMessage: string | null;
  issueDate: string | null;
  createdAt: string;
};

export type SnapshotRow = {
  id: number;
  joinKey: string;
  orderNo: string | null;
  productOrderNo: string | null;
  matchStatus: string;
  orderRowId: number | null;
  feeRowId: number | null;
  orderUploadId: number | null;
  feeUploadId: number | null;
  productName: string | null;
  optionName: string | null;
  sellerProductCode: string | null;
  sellerOptionCode: string | null;
  paidAt: string | null;
  settlementDate: string | null;
  orderSettlementAmount: number | null;
  orderCommissionAmount: number | null;
  orderNetAmount: number | null;
  feeSettlementAmount: number | null;
  feeCommissionAmount: number | null;
  feeNetAmount: number | null;
  resolvedSettlementAmount: number | null;
  resolvedCommissionAmount: number | null;
  resolvedNetAmount: number | null;
  settlementAmountMatched: boolean;
  commissionAmountMatched: boolean;
  netAmountMatched: boolean;
  issueCount: number;
  reviewStatus: string;
  lastAggregatedAt: string;
};

export type DailyRow = {
  id: number;
  uploadId: number;
  settlementCompletedDate: string | null;
  settlementAmount: number | null;
  paymentAmount: number | null;
  productSalesAmount: number | null;
  sellerBurdenDiscountAmount: number | null;
  deliveryFeeAmount: number | null;
  sellerBurdenReturnShippingFee: number | null;
  pointUsageAmount: number | null;
  commissionAmount: number | null;
  claimAmount: number | null;
  settlementMethod: string | null;
  benefitSettlementAmount: number | null;
  dailyDeductionRefundAmount: number | null;
  bizWalletOffsetAmount: number | null;
  safeReturnCareCost: number | null;
  fastSettlementAmount: number | null;
  preferredFeeRefundAmount: number | null;
  createdAt: string;
};

export type MonthlyRow = {
  month: string;
  settlementAmount: number;
  orderCount: number;
};

export type OrderRow = {
  id: number;
  uploadId: number;
  orderNo: string | null;
  productOrderNo: string | null;
  productName: string | null;
  optionName: string | null;
  settlementDate: string | null;
  settlementAmount: number | null;
  commissionAmount: number | null;
  netAmount: number | null;
};

export type FeeRow = {
  id: number;
  uploadId: number;
  orderNo: string | null;
  productOrderNo: string | null;
  feeType: string | null;
  settlementDate: string | null;
  settlementAmount: number | null;
  commissionAmount: number | null;
  netAmount: number | null;
};

export type WorkspaceOwnerType = "GUEST" | "USER";
export type WorkspaceStatus = "ACTIVE" | "SAVED" | "EXPIRED" | "DELETED";

export type WorkspaceSession = {
  workspaceKey: string;
  workspaceToken: string;
  savedAnalysisSetId: number | null;
};

export type WorkspaceFileResponse = {
  workspaceFileId: number;
  uploadId: number;
  originalFileName: string;
  fileType: SettlementFileType;
  active: boolean;
  createdAt: string;
};

export type WorkspaceResponse = {
  workspaceKey: string;
  ownerType: WorkspaceOwnerType;
  status: WorkspaceStatus;
  savedAnalysisSetId: number | null;
  expiresAt: string;
  files: WorkspaceFileResponse[];
  context: AnalysisContextResponse;
  capability: AnalysisCapabilityResponse;
  workspaceToken?: string;
};

export type WorkspaceSaveResponse = {
  message: string;
  workspaceKey: string;
  analysisSetId: number;
};