package com.example.menu.security.model.service;

import java.util.List;
import java.util.Map;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.menu.security.model.dao.AuthDao;
import com.example.menu.security.model.dto.CustomOAuth2User;
import com.example.menu.security.model.dto.AuthDto.User;
import com.example.menu.security.model.dto.AuthDto.UserAuthority;
import com.example.menu.security.model.dto.AuthDto.UserIdentities;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OAuth2Service implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final AuthDao authDao;

    @Transactional
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = new DefaultOAuth2UserService().loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String provider = userRequest.getClientRegistration().getRegistrationId();
        String providerUserId = String.valueOf(attributes.get("id"));
        String accessToken = userRequest.getAccessToken().getTokenValue();

        if (provider.equals("kakao")) {
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            String email = (String) kakaoAccount.get("email");
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

            Map<String, Object> param = Map.of("provider", provider, "providerId", providerUserId);
            User user = authDao.findUserByProvider(param);

            if (user == null) {
                user = User.builder()
                        .email(email)
                        .name((String) profile.get("nickname"))
                        .profile((String) profile.get("profile_image_url"))
                        .build();
                authDao.insertUser(user);

                authDao.insertUserIdentities(UserIdentities.builder()
                        .provider(provider)
                        .providerUserId(providerUserId)
                        .accessToken(accessToken)
                        .userId(user.getId())
                        .build());

                authDao.insertUserRole(UserAuthority.builder()
                        .userId(user.getId())
                        .roles(List.of("ROLE_USER"))
                        .build());
            } else {
                authDao.updateUserIdentities(UserIdentities.builder()
                        .provider(provider)
                        .providerUserId(providerUserId)
                        .accessToken(accessToken)
                        .build());
            }

            return new CustomOAuth2User(
                    oAuth2User.getAuthorities(),
                    attributes,
                    "id",
                    user.getId()
            );
        }
        return oAuth2User;
    }
}