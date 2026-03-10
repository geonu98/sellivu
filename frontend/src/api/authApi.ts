import { http } from "./http";
import type { AuthUser } from "../store/authStore";

export interface AuthResponse extends AuthUser {
  accessToken: string;
}

export interface RefreshResponse {
  accessToken: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface SignUpRequest {
  email: string;
  password: string;
  name: string;
}

export const authApi = {
  async login(payload: LoginRequest): Promise<AuthResponse> {
    const response = await http.post("/api/auth/login", payload);
    return response.data;
  },

  async signup(payload: SignUpRequest): Promise<AuthResponse> {
    const response = await http.post("/api/auth/signup", payload);
    return response.data;
  },

  async me(): Promise<AuthUser> {
    const response = await http.get("/api/users/me");
    return response.data;
  },

  async refresh(): Promise<RefreshResponse> {
    const response = await http.post("/api/auth/refresh");
    return response.data;
  },

  async logout(): Promise<void> {
    await http.post("/api/auth/logout");
  },
};