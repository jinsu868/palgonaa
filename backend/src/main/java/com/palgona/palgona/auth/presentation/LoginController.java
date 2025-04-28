package com.palgona.palgona.auth.presentation;

import com.palgona.palgona.auth.application.LoginService;
import com.palgona.palgona.auth.domain.AuthTokens;
import com.palgona.palgona.auth.dto.request.LoginRequest;
import com.palgona.palgona.auth.dto.response.AccessTokenResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/auth")
public class LoginController {

    private static final int ONE_WEEK_SECONDS = 604800;
    private static final String REFRESH_TOKEN = "refresh_token";
    private static final String ACCESS_TOKEN = "access_token";

    private final LoginService loginService;

    @PostMapping("/login")
    public ResponseEntity<AccessTokenResponse> login(
            @RequestBody LoginRequest loginRequest,
            HttpServletResponse response
    ) {
        AuthTokens authTokens = loginService.login(loginRequest);

        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN, authTokens.refreshToken())
                .maxAge(ONE_WEEK_SECONDS)
//                .secure(true)
                .httpOnly(true)
                .sameSite("None")
                .domain(".palgona.co.kr")
                .path("/")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.ok(new AccessTokenResponse(authTokens.accessToken()));
    }

    @PostMapping("/reissue")
    public ResponseEntity<AccessTokenResponse> reissueToken(
            @CookieValue("refresh-token") String refreshToken,
            @RequestHeader("Authorization") String authHeader
    ) {
        String reissuedToken = loginService.reissueAccessToken(refreshToken, authHeader);
        return ResponseEntity.ok(new AccessTokenResponse(reissuedToken));
    }

    @PostMapping(value = "/logout")
    public ResponseEntity<Void> logout(
            @CookieValue("refresh-token") String refreshToken
    ) {

        loginService.logout(refreshToken);
        return ResponseEntity.noContent().build();
    }
}
