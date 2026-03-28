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

  displayCategory: string;
  title: string;
  description: string;
  impact: string;
  actionGuide: string;
  statusLabel: string;
  explainable: boolean;
  refundCandidate: boolean;
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

  hasIssue: boolean;
  issueCount: number;
  issueMask: number;
  primaryIssueCode: string | null;
  refundCandidate: boolean;
  needsUserInput: boolean;
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
  yearMonth: string;
  settlementAmount: number;
  generalSettlementAmount: number;
  fastSettlementAmount: number;
  settlementBaseAmount: number;
  totalFeeAmount: number;
  benefitSettlementAmount: number;
  dailyDeductionRefundAmount: number;
  holdAmount: number;
  bizWalletOffsetAmount: number;
  safeReturnCareCost: number;
  preferredFeeRefundAmount: number;
  rowCount: number;
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

export type PagedResponse<T> = {
  items: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  hasNext: boolean;
};

export type SettlementRunSummaryResponse = {
  dailyCount: number;
  monthlyCount: number;
  orderCount: number;
  feeCount: number;
  snapshotCount: number;
  issueCount: number;

  settlementAmount: number;
  generalSettlementAmount: number;
  fastSettlementAmount: number;
  settlementBaseAmount: number;
  totalFeeAmount: number;
  benefitSettlementAmount: number;
  dailyDeductionRefundAmount: number;
  holdAmount: number;
  bizWalletOffsetAmount: number;
  safeReturnCareCost: number;
  preferredFeeRefundAmount: number;
};