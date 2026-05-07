package com.kh.menu.security.model.provider;

import java.security.Key;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.kh.menu.security.utils.CookieUtil;

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
			String refreshSecretBase64
			) { // 토큰 서명에 사용하는 key값들 초기화
		byte[] keyBytes= Decoders.BASE64.decode(secretBase64);
		this.key = Keys.hmacShaKeyFor(keyBytes);
		this.refreshKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(refreshSecretBase64));
	}
	
	public String createAccessToken(Long id, int minutes) {
		Date now = new Date();
		return Jwts.builder()
				.setSubject(String.valueOf(id))// 페이로드에 저장할 값.(사용자 id)
				.setIssuedAt(now)
				//.setExpiration(new Date(now.getTime() + (1000L * 60 * minutes)))
				.setExpiration(new Date(now.getTime() + (1000L * 10)))
				.signWith(key, SignatureAlgorithm.HS256)
				.compact();
	}
	
	/* 
	 * Refresh Token
	 *  - accessToken을 새로 갱신받기 위한 용도의 토큰
	 *  - accessToken보다 훨씬 긴 유효시간을 가지고 있다.
	 *  
	 * */
	public String createRefreshToken(Long id, int i) {
		Date now = new Date();
		return Jwts.builder()
				.setSubject(String.valueOf(id))
				.setIssuedAt(now)
				.setExpiration(new Date(now.getTime()+(1000L * 60 * 60 * 24 * i )))
				.signWith(refreshKey, SignatureAlgorithm.HS256)
				.compact();
	}
	
	public Long getUserId(String token, String cookieKey) {
		return Long.valueOf(
				Jwts.parserBuilder()
					.setSigningKey
					(cookieKey.equals(CookieUtil.ACCESS_COOKIE) ? key : refreshKey)
					.build()
					.parseClaimsJws(token)
					.getBody()
					.getSubject()
				);
	}
	
	
	

}








