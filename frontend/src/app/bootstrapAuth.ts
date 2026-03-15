import { authApi } from "../api/authApi";
import { useAuthStore } from "../store/authStore";

function isUnauthorized(error: unknown) {
  const status = (error as any)?.response?.status;
  return status === 401;
}

export async function bootstrapAuth() {
  const store = useAuthStore.getState();

  try {
    const refreshResponse = await authApi.refresh();
    store.setAccessToken(refreshResponse.accessToken);
    store.setAuthenticated(true);

    const me = await authApi.me();
    store.setUser(me);
  } catch (error) {
    // guest 상태에서는 refresh 실패가 정상일 수 있음
    if (!isUnauthorized(error)) {
      console.error("bootstrapAuth 실패:", error);
    }

    store.clearAuth();
  } finally {
    store.setAuthInitialized(true);
  }
}