import { useState } from "react";
import { authApi } from "../../api/authApi";
import { useAuthStore } from "../../store/authStore";

type Mode = "login" | "signup";

export default function AuthModal() {
  const authModalOpen = useAuthStore((state) => state.authModalOpen);
  const closeAuthModal = useAuthStore((state) => state.closeAuthModal);
  const setAccessToken = useAuthStore((state) => state.setAccessToken);
  const setUser = useAuthStore((state) => state.setUser);

  const [mode, setMode] = useState<Mode>("login");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [name, setName] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  if (!authModalOpen) return null;

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setSubmitting(true);
    setErrorMessage("");

    try {
      const response =
        mode === "login"
          ? await authApi.login({ email, password })
          : await authApi.signup({ email, password, name });

      setAccessToken(response.accessToken);
      setUser({
        userId: response.userId,
        email: response.email,
        name: response.name,
        role: response.role,
      });

      closeAuthModal();
      setPassword("");
    } catch (error: any) {
      setErrorMessage(
        error?.response?.data?.message ?? "인증 처리 중 오류가 발생했습니다."
      );
    } finally {
      setSubmitting(false);
    }
  }

  function handleClose() {
    if (submitting) return;
    closeAuthModal();
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 px-4">
      <div className="w-full max-w-md rounded-2xl bg-white p-6 shadow-xl">
        <div className="mb-4 flex items-start justify-between">
          <div>
            <h2 className="text-lg font-semibold">
              {mode === "login" ? "로그인" : "회원가입"}
            </h2>
            <p className="mt-1 text-sm text-slate-500">
              비로그인 상태에서도 분석은 가능하지만,
              <br />
              저장 및 이전 작업 조회는 로그인 후 사용할 수 있습니다.
            </p>
          </div>

          <button
            type="button"
            className="rounded-md px-2 py-1 text-sm text-slate-500 hover:bg-slate-100"
            onClick={handleClose}
            disabled={submitting}
          >
            닫기
          </button>
        </div>

        <div className="mb-4 flex rounded-lg bg-slate-100 p-1">
          <button
            type="button"
            className={`flex-1 rounded-md px-3 py-2 text-sm ${
              mode === "login"
                ? "bg-white font-medium shadow-sm"
                : "text-slate-500"
            }`}
            onClick={() => setMode("login")}
            disabled={submitting}
          >
            로그인
          </button>
          <button
            type="button"
            className={`flex-1 rounded-md px-3 py-2 text-sm ${
              mode === "signup"
                ? "bg-white font-medium shadow-sm"
                : "text-slate-500"
            }`}
            onClick={() => setMode("signup")}
            disabled={submitting}
          >
            회원가입
          </button>
        </div>

        <form className="space-y-3" onSubmit={handleSubmit}>
          {mode === "signup" && (
            <div>
              <label className="mb-1 block text-sm font-medium">이름</label>
              <input
                className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm outline-none focus:border-black"
                value={name}
                onChange={(e) => setName(e.target.value)}
                placeholder="이름을 입력하세요"
                disabled={submitting}
              />
            </div>
          )}

          <div>
            <label className="mb-1 block text-sm font-medium">이메일</label>
            <input
              type="email"
              className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm outline-none focus:border-black"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="이메일을 입력하세요"
              disabled={submitting}
            />
          </div>

          <div>
            <label className="mb-1 block text-sm font-medium">비밀번호</label>
            <input
              type="password"
              className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm outline-none focus:border-black"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="비밀번호를 입력하세요"
              disabled={submitting}
            />
          </div>

          {errorMessage && (
            <div className="rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-600">
              {errorMessage}
            </div>
          )}

          <button
            type="submit"
            className="w-full rounded-lg bg-black px-4 py-3 text-sm font-medium text-white disabled:opacity-50"
            disabled={submitting}
          >
            {submitting
              ? mode === "login"
                ? "로그인 중..."
                : "가입 중..."
              : mode === "login"
                ? "로그인"
                : "회원가입"}
          </button>
        </form>
      </div>
    </div>
  );
}