"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { fetchWithAuth } from "@/lib/api";

export default function OAuth2SuccessPage() {
  const router = useRouter();

  useEffect(() => {
    const fetchUser = async () => {
      try {
        // 카카오 로그인 성공 후 백엔드가 쿠키를 세팅한 상태로 이 페이지로 리다이렉트 됨
        // 쿠키에 accessToken, refreshToken, userRoles가 이미 담겨있으므로 바로 /auth/me 호출
        const res = await fetchWithAuth("/auth/me");

        if (res.ok) {
          router.replace("/dashboard");
        } else {
          router.replace("/login");
        }
      } catch (error) {
        console.error("OAuth2 success handling failed:", error);
        router.replace("/login");
      }
    };

    fetchUser();
  }, [router]);

  return (
    <div className="flex min-h-screen items-center justify-center bg-gray-50">
      <div className="flex flex-col items-center gap-4">
        <div className="h-10 w-10 animate-spin rounded-full border-4 border-gray-300 border-t-yellow-400" />
        <p className="text-gray-600 font-medium">카카오 로그인 처리 중...</p>
        <p className="text-sm text-gray-400">잠시만 기다려주세요</p>
      </div>
    </div>
  );
}