"use client";

import { useState, Suspense } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { useAuth } from "@/context/AuthContext";

function LoginForm() {
  const [email, setEmail]         = useState("");
  const [password, setPassword]   = useState("");
  const [error, setError]         = useState("");
  const [isLoading, setIsLoading] = useState(false);

  const { login }    = useAuth();
  const router       = useRouter();
  const searchParams = useSearchParams();

  // middleware 가 redirect 파라미터로 원래 경로를 전달함
  const redirectTo = searchParams.get("redirect") || "/dashboard";

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");
    setIsLoading(true);
    try {
      await login(email, password);
      router.push(redirectTo);
    } catch (err: unknown) {
      if (err instanceof Error) {
        setError(err.message);
        if (err.message.includes("존재하지 않는")) {
          const goSignup = window.confirm(
            "존재하지 않는 이메일입니다.\n회원가입 페이지로 이동하시겠습니까?"
          );
          if (goSignup) router.push(`/signup?email=${encodeURIComponent(email)}`);
        }
      }
    } finally {
      setIsLoading(false);
    }
  };

  const handleKakaoLogin = () => {
    window.location.href = "http://localhost:8081/api/oauth2/authorization/kakao";
  };

  return (
    <div className="flex min-h-screen items-center justify-center bg-gray-50">
      <div className="w-full max-w-md rounded-2xl bg-white p-8 shadow-lg">
        <h1 className="mb-2 text-3xl font-bold text-center text-gray-900">로그인</h1>
        <p className="mb-8 text-center text-sm text-gray-500">FlowerBark에 오신 것을 환영합니다</p>

        {error && (
          <div className="mb-4 rounded-lg bg-red-50 p-3 text-red-700 text-sm border border-red-200">
            {error}
          </div>
        )}

        <form onSubmit={handleLogin} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">이메일</label>
            <input
              type="email" value={email} onChange={(e) => setEmail(e.target.value)}
              className="w-full rounded-lg border border-gray-300 px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition"
              placeholder="example@email.com" required autoComplete="email"
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">비밀번호</label>
            <input
              type="password" value={password} onChange={(e) => setPassword(e.target.value)}
              className="w-full rounded-lg border border-gray-300 px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition"
              placeholder="비밀번호를 입력하세요" required autoComplete="current-password"
            />
          </div>
          <button type="submit" disabled={isLoading}
            className="w-full rounded-lg bg-blue-600 py-2.5 text-sm text-white font-semibold hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition">
            {isLoading ? (
              <span className="flex items-center justify-center gap-2">
                <span className="h-4 w-4 animate-spin rounded-full border-2 border-white border-t-transparent" />
                로그인 중...
              </span>
            ) : "로그인"}
          </button>
        </form>

        <div className="my-6 flex items-center gap-3">
          <div className="flex-1 h-px bg-gray-200" />
          <span className="text-xs text-gray-400">또는</span>
          <div className="flex-1 h-px bg-gray-200" />
        </div>

        <button onClick={handleKakaoLogin}
          className="w-full rounded-lg bg-yellow-400 py-2.5 text-sm text-gray-800 font-semibold hover:bg-yellow-500 transition flex items-center justify-center gap-2">
          카카오로 로그인
        </button>

        <p className="mt-6 text-center text-sm text-gray-500">
          계정이 없으신가요?{" "}
          <a href="/signup" className="text-blue-600 font-medium hover:underline">회원가입</a>
        </p>
      </div>
    </div>
  );
}

export default function LoginPage() {
  return (
    <Suspense fallback={<div className="flex min-h-screen items-center justify-center"><div className="h-8 w-8 animate-spin rounded-full border-4 border-gray-300 border-t-blue-600" /></div>}>
      <LoginForm />
    </Suspense>
  );
}