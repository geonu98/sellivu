import axios, {
  AxiosError,
  type AxiosRequestConfig,
  type InternalAxiosRequestConfig,
} from "axios";
import { useAuthStore } from "../store/authStore";

type RetryableRequestConfig = InternalAxiosRequestConfig & {
  _retry?: boolean;
};

export const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  withCredentials: true,
});

const authFreePaths = ["/api/auth/login", "/api/auth/signup", "/api/auth/refresh"];

function isAuthFreeRequest(url?: string) {
  if (!url) return false;
  return authFreePaths.some((path) => url.includes(path));
}

let refreshPromise: Promise<string | null> | null = null;

async function requestRefresh(): Promise<string | null> {
  if (!refreshPromise) {
    refreshPromise = (async () => {
      try {
        const response = await http.post("/api/auth/refresh");
        const newAccessToken = response.data?.accessToken ?? null;

        if (!newAccessToken) {
          throw new Error("refresh 응답에 accessToken이 없습니다.");
        }

        const store = useAuthStore.getState();
        store.setAccessToken(newAccessToken);
        store.setAuthenticated(true);

        return newAccessToken;
      } catch {
        useAuthStore.getState().clearAuth();
        return null;
      } finally {
        refreshPromise = null;
      }
    })();
  }

  return refreshPromise;
}

http.interceptors.request.use(
  (config) => {
    const { accessToken } = useAuthStore.getState();

    if (accessToken) {
      config.headers.Authorization = `Bearer ${accessToken}`;
    }

    return config;
  },
  (error) => Promise.reject(error)
);

http.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const status = error.response?.status;
    const originalRequest = error.config as RetryableRequestConfig | undefined;

    if (!originalRequest) {
      return Promise.reject(error);
    }

    if (status !== 401) {
      return Promise.reject(error);
    }

    if (isAuthFreeRequest(originalRequest.url)) {
      return Promise.reject(error);
    }

    const { accessToken, isAuthenticated } = useAuthStore.getState();

    // 게스트 요청이면 refresh 시도하지 않음
    if (!accessToken || !isAuthenticated) {
      return Promise.reject(error);
    }

    if (originalRequest._retry) {
      useAuthStore.getState().clearAuth();
      return Promise.reject(error);
    }

    originalRequest._retry = true;

    const newAccessToken = await requestRefresh();

    if (!newAccessToken) {
      return Promise.reject(error);
    }

    originalRequest.headers = originalRequest.headers ?? {};
    originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;

    return http(originalRequest as AxiosRequestConfig);
  }
);