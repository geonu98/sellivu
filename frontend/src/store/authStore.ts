import { create } from "zustand";

export type UserRole = "USER" | "ADMIN";

export interface AuthUser {
  userId: number;
  email: string;
  name: string;
  role: UserRole;
}

interface AuthState {
  accessToken: string | null;
  user: AuthUser | null;
  isAuthenticated: boolean;
  isAuthInitialized: boolean;
  authModalOpen: boolean;

  setAccessToken: (token: string | null) => void;
  setUser: (user: AuthUser | null) => void;
  setAuthenticated: (value: boolean) => void;
  setAuthInitialized: (value: boolean) => void;
  openAuthModal: () => void;
  closeAuthModal: () => void;
  clearAuth: () => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  accessToken: null,
  user: null,
  isAuthenticated: false,
  isAuthInitialized: false,
  authModalOpen: false,

  setAccessToken: (token) =>
    set((state) => ({
      accessToken: token,
      isAuthenticated: !!token || !!state.user,
    })),

  setUser: (user) =>
    set({
      user,
      isAuthenticated: !!user,
    }),

  setAuthenticated: (value) =>
    set({
      isAuthenticated: value,
    }),

  setAuthInitialized: (value) =>
    set({
      isAuthInitialized: value,
    }),

  openAuthModal: () =>
    set({
      authModalOpen: true,
    }),

  closeAuthModal: () =>
    set({
      authModalOpen: false,
    }),

  clearAuth: () =>
    set({
      accessToken: null,
      user: null,
      isAuthenticated: false,
    }),
}));