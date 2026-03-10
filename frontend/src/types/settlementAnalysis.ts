export type SettlementFileType =
  | "DAILY_SETTLEMENT"
  | "ORDER_SETTLEMENT"
  | "FEE_DETAIL";

export type YesNoUnknown = "YES" | "NO" | "UNKNOWN";

export type AvailableView =
  | "OVERVIEW"
  | "ISSUES"
  | "SNAPSHOTS"
  | "DAILY"
  | "MONTHLY"
  | "ORDER_DETAIL"
  | "FEE_DETAIL"
  | "ORDER_FEE_CROSS_CHECK";

export interface AnalysisSetResponse {
  id: number;
  name: string;
  createdAt: string;
  updatedAt?: string;
}

export interface CreateAnalysisSetRequest {
  name: string;
}

export interface AnalysisSetItemResponse {
  id: number;
  analysisSetId: number;
  uploadId: number;
  fileType: SettlementFileType;
  createdAt: string;
}

export interface AnalysisCapabilityResponse {
  analysisSetId: number;
  uploadedFileTypes: SettlementFileType[];
  availableViews: AvailableView[];
  missingFileTypes: SettlementFileType[];
  gaps: string[];
  requiresContextOptions: boolean;
  explainablePolicyFactors: string[];
  verificationPendingFields: string[];
  message: string;
}

export interface AnalysisContextResponse {
  analysisSetId: number;
  storeCouponUsage: YesNoUnknown;
  naverCouponUsage: YesNoUnknown;
  pointBenefitUsage: YesNoUnknown;
  safeReturnCareUsage: YesNoUnknown;
  bizWalletOffsetUsage: YesNoUnknown;
  fastSettlementUsage: YesNoUnknown;
  claimIncluded: YesNoUnknown;
  updatedAt: string;
}

export interface UpdateAnalysisContextRequest {
  storeCouponUsage: YesNoUnknown;
  naverCouponUsage: YesNoUnknown;
  pointBenefitUsage: YesNoUnknown;
  safeReturnCareUsage: YesNoUnknown;
  bizWalletOffsetUsage: YesNoUnknown;
  fastSettlementUsage: YesNoUnknown;
  claimIncluded: YesNoUnknown;
}

export interface RebuildAnalysisResponse {
  message: string;
  analysisSetId: number;
  rebuiltUploadCount: number;
  deletedSnapshotCount: number;
}

export type MatchStatus =
  | "MATCHED"
  | "ORDER_ONLY"
  | "FEE_ONLY"
  | "MISMATCHED";

export type ReviewStatus = "OK" | "REVIEW" | "ISSUE";

export interface SnapshotRow {
  snapshotId: number;
  joinKey: string;
  orderNo: string | null;
  productOrderNo: string | null;
  productName: string | null;
  settlementDate: string | null;
  matchStatus: MatchStatus;
  resolvedSettlementAmount: number | null;
  resolvedCommissionAmount: number | null;
  resolvedNetAmount: number | null;
  issueCount: number;
  reviewStatus: ReviewStatus;
}

export type IssueSourceType = "SNAPSHOT" | "DAILY_CROSS_CHECK";
export type Severity = "ERROR" | "WARN" | "INFO";
export type JudgementStatus = "CONFIRMED" | "EXPLAINABLE" | "PENDING";

export interface IssueRow {
  id: number;
  sourceType: IssueSourceType;
  issueType: string;
  orderNo: string | null;
  productOrderNo: string | null;
  joinKey: string | null;
  message: string;
  severity: Severity;
  judgementStatus: JudgementStatus;
  explanationCode: string | null;
  needsUserInput: boolean;
  possibleReasonMessage: string | null;
  issueDate: string | null;
  createdAt: string;
}

export interface DailyRow {
  id: number;
  settlementDate: string | null;
  settlementAmount: number | null;
  benefitAmount: number | null;
  deductionRefundAmount: number | null;
  bizWalletOffsetAmount: number | null;
  safeReturnCareAmount: number | null;
  preferredFeeRefundAmount: number | null;
  settlementMethod: string | null;
}

export interface MonthlyRow {
  month: string;
  settlementAmount: number | null;
  benefitAmount: number | null;
  deductionRefundAmount: number | null;
  netAmount: number | null;
}

export interface OrderRow {
  id: number;
  orderNo: string | null;
  productOrderNo: string | null;
  productName: string | null;
  settlementDate: string | null;
  settlementAmount: number | null;
  benefitAmount: number | null;
  netAmount: number | null;
}

export interface FeeRow {
  id: number;
  orderNo: string | null;
  productOrderNo: string | null;
  productName: string | null;
  settlementDate: string | null;
  feeAmount: number | null;
  feeType: string | null;
  netAmount: number | null;
}

export interface SettlementUploadResponse {
  uploadId: number;
  fileName: string;
  fileType: SettlementFileType;
  message?: string;
}