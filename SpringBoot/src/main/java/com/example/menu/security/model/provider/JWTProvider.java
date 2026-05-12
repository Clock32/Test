package com.example.menu.security.model.provider;

import java.security.Key;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.example.menu.security.utils.CookieUtil;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JWTProvider {

	private final Key key;
	private final Key refreshKey;

	public JWTProvider(
			@Value("${jwt.secret}")
			String secretBase64,
			@Value("${jwt.refresh-secret}")
			String refreshSecretBase64) {
		byte[] keyBytes = Decoders.BASE64.decode(secretBase64);
		this.key = Keys.hmacShaKeyFor(keyBytes);
		this.refreshKey = Keys.hmacShaKeyFor(
				Decoders.BASE64.decode(refreshSecretBase64));
	}

	// ✅ 수정: 하드코딩 1000L * 10 (10초) → 1000L * 60 * minutes (분 단위)
	// 호출 시 minutes=30 전달 → 30분 만료
	public String createAccessToken(Long id, int minutes) {
		Date now = new Date();
		return Jwts.builder()
				.setSubject(String.valueOf(id))
				.setIssuedAt(now)
				.setExpiration(new Date(now.getTime() + (1000L * 60 * minutes)))
				.signWith(key, SignatureAlgorithm.HS256)
				.compact();
	}

	// refreshToken: days 단위 (7일)
	public String createRefreshToken(Long id, int days) {
		Date now = new Date();
		return Jwts.builder()
				.setSubject(String.valueOf(id))
				.setIssuedAt(now)
				.setExpiration(new Date(now.getTime() + (1000L * 60 * 60 * 24 * days)))
				.signWith(refreshKey, SignatureAlgorithm.HS256)
				.compact();
	}

	public Long getUserId(String token, String cookieKey) {
		return Long.valueOf(
				Jwts.parserBuilder()
						.setSigningKey(
								cookieKey.equals(CookieUtil.ACCESS_COOKIE) ? key : refreshKey)
						.build()
						.parseClaimsJws(token)
						.getBody()
						.getSubject());
	}
}