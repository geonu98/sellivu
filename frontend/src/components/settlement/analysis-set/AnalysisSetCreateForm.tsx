import { useState } from "react";

type Props = {
  onCreate: (name: string) => Promise<void>;
};

export default function AnalysisSetCreateForm({ onCreate }: Props) {
  const [name, setName] = useState("");
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!name.trim()) return;

    setLoading(true);
    try {
      await onCreate(name.trim());
      setName("");
    } finally {
      setLoading(false);
    }
  }

  return (
    <form onSubmit={handleSubmit} className="rounded-xl border bg-white p-4">
      <h3 className="mb-3 text-sm font-semibold">분석 세트 생성</h3>
      <div className="flex gap-2">
        <input
          className="flex-1 rounded-lg border px-3 py-2 text-sm"
          placeholder="예: 3월 정산 검증"
          value={name}
          onChange={(e) => setName(e.target.value)}
        />
        <button
          type="submit"
          disabled={loading}
          className="rounded-lg bg-black px-4 py-2 text-sm text-white disabled:opacity-50"
        >
          {loading ? "생성 중..." : "생성"}
        </button>
      </div>
    </form>
  );
}