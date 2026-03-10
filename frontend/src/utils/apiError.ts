import type { ApiErrorResponse } from "../types/api";

export function extractApiError(error: unknown): ApiErrorResponse | null {
  const apiError = (error as any)?.response?.data;

  if (!apiError || typeof apiError !== "object") {
    return null;
  }

  return apiError as ApiErrorResponse;
}

export function getApiErrorMessage(error: unknown): string {
  const apiError = extractApiError(error);

  switch (apiError?.code) {
    case "ANALYSIS_SET_NOT_FOUND":
      return "해당 분석 세트를 찾을 수 없습니다.";
    case "SETTLEMENT_UPLOAD_NOT_FOUND":
      return "업로드 파일을 찾을 수 없습니다.";
    case "ANALYSIS_SET_UPLOAD_ALREADY_EXISTS":
      return "이미 해당 분석 세트에 연결된 파일입니다.";
    case "SETTLEMENT_SNAPSHOT_NOT_FOUND":
      return "정산 스냅샷을 찾을 수 없습니다.";
    case "INVALID_ANALYSIS_CONTEXT_OPTION":
      return "옵션 값이 올바르지 않습니다.";
    case "DUPLICATE_SETTLEMENT_UPLOAD":
      return "이미 업로드된 파일입니다.";
    case "BAD_REQUEST":
      return "잘못된 요청입니다.";
    case "VALIDATION_ERROR":
      return "입력값을 다시 확인해주세요.";
    case "UNSUPPORTED_MEDIA_TYPE":
      return "지원하지 않는 파일 형식입니다.";
    case "INTERNAL_SERVER_ERROR":
      return "서버 오류가 발생했습니다.";
    default:
      return apiError?.message ?? "요청 처리 중 오류가 발생했습니다.";
  }
}