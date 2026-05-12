package com.example.menu.security.model.handler;

import java.io.IOException;
import java.util.stream.Collectors;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.menu.security.model.dto.CustomOAuth2User;
import com.example.menu.security.model.provider.JWTProvider;
import com.example.menu.security.utils.CookieUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JWTProvider jwt;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        
        CustomOAuth2User oauthUser = (CustomOAuth2User) authentication.getPrincipal();
        Long id = oauthUser.getUserId();
        
        // 토큰 생성
        String accessToken = jwt.createAccessToken(id, 30);
        String refreshToken = jwt.createRefreshToken(id, 7);
        String roles = oauthUser.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining("|"));
        
        // 기존 CookieUtil을 사용한 쿠키 생성
        ResponseCookie accessCookie = CookieUtil.createTokenCookie(CookieUtil.ACCESS_COOKIE, accessToken, 30);
        ResponseCookie refreshCookie = CookieUtil.createTokenCookie(CookieUtil.REFERSH_COOKIE, refreshToken, 60 * 24 * 7); // 7일
        ResponseCookie roleCookie = CookieUtil.createTokenCookie(CookieUtil.ROLE_COOKIE, roles, 30);
        
        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, roleCookie.toString());
        
        // Next.js 콜백 경로로 리다이렉트
        String redirect = UriComponentsBuilder
                .fromUriString("http://localhost:3000/oauth2/success")
                .build().toUriString();
        
        log.info("OAuth2 Success: Redirecting to {}", redirect);
        response.sendRedirect(redirect);
    }
}