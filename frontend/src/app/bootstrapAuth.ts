import { authApi } from "../api/authApi";
import { useAuthStore } from "../store/authStore";

export async function bootstrapAuth() {
  const store = useAuthStore.getState();

  try {
    const refreshResponse = await authApi.refresh();
    store.setAccessToken(refreshResponse.accessToken);
    store.setAuthenticated(true);

    const me = await authApi.me();
    store.setUser(me);
  } catch (error) {
    store.clearAuth();
  } finally {
    store.setAuthInitialized(true);
  }
}