package com.example.menu.security.utils;

import java.time.Duration;

import org.springframework.http.ResponseCookie;

import jakarta.servlet.http.HttpServletRequest;

public class CookieUtil {

	public static final String ACCESS_COOKIE  = "accessToken";
	public static final String REFERSH_COOKIE = "refreshToken";
	public static final String ROLE_COOKIE    = "userRoles";

	/**
	 * 토큰 쿠키 생성
	 *
	 * @param name       쿠키 이름 (ACCESS_COOKIE / REFERSH_COOKIE / ROLE_COOKIE)
	 * @param value      쿠키 값
	 * @param maxAge     만료 크기
	 *                   - REFERSH_COOKIE : days 단위 (7일)
	 *                   - ACCESS_COOKIE / ROLE_COOKIE : minutes 단위 (30분)
	 *                   - 0 이면 즉시 만료 (로그아웃)
	 */
	public static ResponseCookie createTokenCookie(
			String name, String value, long maxAge) {

		return ResponseCookie.from(name, value)
				// refreshToken 만 httpOnly=true (JS 접근 차단)
				.httpOnly(name.equals(REFERSH_COOKIE))
				.secure(false)   // 개발환경: false / 운영환경(HTTPS): true 로 변경
				.path("/")
				.sameSite("Lax") // CSRF 방어
				.maxAge(
					maxAge == 0
						? Duration.ZERO                        // 즉시 만료 (로그아웃)
						: name.equals(REFERSH_COOKIE)
							? Duration.ofDays(maxAge)          // ✅ refreshToken: 일(day) 단위
							: Duration.ofMinutes(maxAge)       // ✅ accessToken/roles: 분(minute) 단위 (수정됨)
				)
				.build();
	}

	/**
	 * Authorization 헤더에서 Bearer 토큰 추출
	 */
	public static String resolveAccessToken(HttpServletRequest req) {
		String bearerToken = req.getHeader("Authorization");
		if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
			return bearerToken.substring(7).trim();
		}
		return null;
	}
}