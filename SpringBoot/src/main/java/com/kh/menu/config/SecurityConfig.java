package com.kh.menu.config;

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

import com.kh.menu.security.filter.JWTAutenticationFilter;
import com.kh.menu.security.model.handler.OAuth2SuccessHandler;
import com.kh.menu.security.model.service.OAuth2Service;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {
	@Bean
	public SecurityFilterChain filterChain(
			HttpSecurity http ,
			JWTAutenticationFilter jwtFilter,
			OAuth2Service service,
			OAuth2SuccessHandler handler
			) throws Exception {
		http
			.cors(cors -> cors.configurationSource(corsConfigurationSource()))
			.csrf(csrf -> csrf.disable())
			.exceptionHandling(e-> e.authenticationEntryPoint((req, res, ex)-> {
				// 인증실패시 401에러
				res.sendError(HttpServletResponse.SC_UNAUTHORIZED,"UNAUTHORIZED");
			})
			.accessDeniedHandler((req, res, ex) -> {
				// 인가실패시 403에러
				res.sendError(HttpServletResponse.SC_FORBIDDEN,"FORBIDDEN");
			}))
			.sessionManagement( management -> 
				management.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			//. oauth2인증설정
			.oauth2Login( oauth -> oauth
					.userInfoEndpoint( u -> u.userService(service))
					.successHandler(handler)					
					)
			.authorizeHttpRequests( auth ->
					auth
					.requestMatchers("/auth/login","/auth/signup","/auth/logout","/auth/refresh").permitAll()
					.requestMatchers("/oauth2/**","/login**","/error").permitAll()
					.requestMatchers("/**").authenticated()
			);
		http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);		
		
		return http.build();		
	}
	
	// CORS설정정보를 가진 빈객체
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration config = new CorsConfiguration();
		
		// 허용 Origin설정
		config.setAllowedOrigins(List.of("http://localhost:3000"));
		
		// 허용 메서드 설정
		config.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE"));
		// 허용 헤더설정
		config.setAllowedHeaders(List.of("*"));
		config.setExposedHeaders(List.of("Location","Authorization"));
		config.setAllowCredentials(true);//세션,쿠키 허용설정
		config.setMaxAge(3600L);
		
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);
		
		return source;
	}
	
	
	@Bean
	public PasswordEncoder passwordEncoder() {
		PasswordEncoder encoder = new BCryptPasswordEncoder();
		return encoder;
	}
	
}





