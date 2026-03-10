import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";
import { useEffect } from "react";
import SettlementAnalysisPage from "./pages/settlement/SettlementAnalysisPage";
import { bootstrapAuth } from "./app/bootstrapAuth";
import { useAuthStore } from "./store/authStore";
import AuthModal from "./components/auth/AuthModal";

export default function App() {
  const isAuthInitialized = useAuthStore((state) => state.isAuthInitialized);

  useEffect(() => {
    bootstrapAuth();
  }, []);

  if (!isAuthInitialized) {
    return <div className="p-6 text-sm text-slate-500">사용자 상태 확인 중...</div>;
  }

  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Navigate to="/settlement-analysis" replace />} />
        <Route path="/settlement-analysis" element={<SettlementAnalysisPage />} />
      </Routes>

      <AuthModal />
    </BrowserRouter>
  );
}