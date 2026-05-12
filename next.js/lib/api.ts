const BASE_URL = "/api";

// 쿠키 읽기 헬퍼 (accessToken은 httpOnly=false 이므로 JS에서 읽기 가능)
export function getCookie(name: string): string | null {
  if (typeof document === "undefined") return null;
  const value = `; ${document.cookie}`;
  const parts = value.split(`; ${name}=`);
  if (parts.length === 2) return parts.pop()?.split(";").shift() || null;
  return null;
}

// accessToken 갱신 (refreshToken 쿠키는 httpOnly=true 이므로 자동 전송됨)
async function refreshAccessToken(): Promise<boolean> {
  try {
    const response = await fetch(`${BASE_URL}/auth/refresh`, {
      method: "POST",
      credentials: "include",
    });
    return response.ok;
  } catch {
    return false;
  }
}

// 공통 인증 fetch 함수
// accessToken을 Authorization 헤더로 전달하고, 401 시 자동 갱신 후 재시도
export async function fetchWithAuth(
  url: string,
  options: RequestInit = {}
): Promise<Response> {
  const accessToken = getCookie("accessToken");

  const headers: HeadersInit = {
    "Content-Type": "application/json",
    ...(accessToken ? { Authorization: `Bearer ${accessToken}` } : {}),
    ...(options.headers as Record<string, string>),
  };

  const response = await fetch(`${BASE_URL}${url}`, {
    ...options,
    headers,
    credentials: "include",
  });

  // 401 → 토큰 갱신 후 재시도
  if (response.status === 401) {
    const refreshed = await refreshAccessToken();
    if (refreshed) {
      const newToken = getCookie("accessToken");
      const retryHeaders: HeadersInit = {
        "Content-Type": "application/json",
        ...(newToken ? { Authorization: `Bearer ${newToken}` } : {}),
        ...(options.headers as Record<string, string>),
      };
      return fetch(`${BASE_URL}${url}`, {
        ...options,
        headers: retryHeaders,
        credentials: "include",
      });
    } else {
      // 갱신 실패 → 로그인 페이지로 이동 (클라이언트 사이드에서만)
      if (typeof window !== "undefined") {
        window.location.href = "/login";
      }
      throw new Error("인증이 만료되었습니다.");
    }
  }

  return response;
}