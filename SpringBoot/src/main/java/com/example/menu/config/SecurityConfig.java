package com.example.menu.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.example.menu.security.filter.JWTAutenticationFilter;
import com.example.menu.security.model.handler.OAuth2SuccessHandler;
import com.example.menu.security.model.service.OAuth2Service;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {

	@Bean
	public SecurityFilterChain filterChain(
			HttpSecurity http,
			JWTAutenticationFilter jwtFilter,
			OAuth2Service service,
			OAuth2SuccessHandler handler) throws Exception {

		http
			.cors(cors -> cors.configurationSource(corsConfigurationSource()))
			.csrf(csrf -> csrf.disable())
			.exceptionHandling(e -> e
				.authenticationEntryPoint((req, res, ex) ->
					res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "UNAUTHORIZED"))
				.accessDeniedHandler((req, res, ex) ->
					res.sendError(HttpServletResponse.SC_FORBIDDEN, "FORBIDDEN"))
			)
			.sessionManagement(m ->
				m.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			// OAuth2 카카오 로그인 설정
			.oauth2Login(oauth -> oauth
				.userInfoEndpoint(u -> u.userService(service))
				.successHandler(handler))
			.authorizeHttpRequests(auth -> auth
				// 인증 없이 허용할 경로
				.requestMatchers(
					"/auth/login",
					"/auth/signup",
					"/auth/logout",
					"/auth/refresh"
				).permitAll()
				.requestMatchers("/oauth2/**", "/login**", "/error").permitAll()
				// 나머지 모든 요청은 인증 필요
				.requestMatchers("/**").authenticated()
			);

		// JWT 필터를 UsernamePasswordAuthenticationFilter 앞에 삽입
		http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	/**
	 * CORS 설정
	 * ✅ 수정: OPTIONS 메서드 추가 (Preflight 요청 허용)
	 * ✅ 수정: Set-Cookie 헤더 노출 추가
	 */
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration config = new CorsConfiguration();

		// Next.js 프론트엔드 도메인만 허용
		config.setAllowedOrigins(List.of("http://localhost:3000"));

		// ✅ OPTIONS 추가: 브라우저가 실제 요청 전 preflight(OPTIONS)를 먼저 보내므로 반드시 필요
		config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

		// 모든 헤더 허용
		config.setAllowedHeaders(List.of("*"));

		// ✅ Set-Cookie 추가: 쿠키 응답 헤더를 브라우저에서 읽을 수 있도록 노출
		config.setExposedHeaders(List.of("Location", "Authorization", "Set-Cookie"));

		// 쿠키/세션 포함 허용 (credentials: "include" 와 대응)
		config.setAllowCredentials(true);

		// preflight 캐시 1시간
		config.setMaxAge(3600L);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);
		return source;
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}