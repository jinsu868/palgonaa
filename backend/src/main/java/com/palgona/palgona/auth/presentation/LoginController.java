package com.palgona.palgona.auth.presentation;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import com.palgona.palgona.common.dto.CustomMemberDetails;
import com.palgona.palgona.common.jwt.util.JwtService;
import com.palgona.palgona.common.jwt.util.TokenExtractor;
import com.palgona.palgona.auth.dto.AuthToken;
import com.palgona.palgona.auth.dto.response.LoginResponse;
import com.palgona.palgona.member.dto.request.MemberCreateRequest;
import com.palgona.palgona.auth.application.LoginService;
import com.palgona.palgona.member.dto.request.MemberCreateRequestWithoutImage;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class LoginController {
    private static final String BEARER = "Bearer ";
    private static final String REFRESH_HEADER = "refresh-token";

    private final LoginService loginService;
    private final JwtService jwtService;
    private final TokenExtractor tokenExtractor;

    @PostMapping(
            value = "/signup",
            consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @Operation(summary = "회원 가입 api", description = "닉네임, 프로필을 받아서 회원가입을 진행한다.")
    public ResponseEntity<Void> create(
            @AuthenticationPrincipal CustomMemberDetails loginMember,
            @RequestPart MemberCreateRequestWithoutImage request,
            @RequestPart(required = false) MultipartFile file
    ) {

        MemberCreateRequest memberCreateRequest = MemberCreateRequest.of(request, file);
        Long memberId = loginService.signUp(loginMember.getMember(), memberCreateRequest);

        return ResponseEntity.created(URI.create("/members/" + memberId))
                .build();
    }

    @GetMapping("/login")
    @Operation(summary = "로그인 api", description = "kakao AccessToken을 받아서 로그인을 진행한다.")
    public LoginResponse login(
            HttpServletRequest request,
            HttpServletResponse response
    ) {

        LoginResponse loginResponse = loginService.login(request);
        String socialId = loginResponse.socialId();
        AuthToken authToken = jwtService.issueToken(socialId);
        response.setHeader(AUTHORIZATION, BEARER + authToken.accessToken());
        response.setHeader(REFRESH_HEADER, BEARER + authToken.refreshToken());

        return loginResponse;
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃 api", description = "access, refresh 토큰을 헤더에 보내서 로그아웃을 진행한다.")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        String refreshToken = tokenExtractor.extractRefreshToken(request);
        String accessToken = tokenExtractor.extractAccessToken(request);
        jwtService.removeRefreshToken(refreshToken);
        loginService.logout(accessToken);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "토큰 재발급 api",
            description = "refresh-token을 통해 토큰을 재발급 받는다.")
    public ResponseEntity<Void> reissueToken(HttpServletRequest request, HttpServletResponse response) {
        String accessToken = tokenExtractor.extractAccessToken(request);
        String refreshToken = tokenExtractor.extractRefreshToken(request);
        AuthToken authToken = jwtService.reissueToken(accessToken, refreshToken);
        response.setHeader(AUTHORIZATION, BEARER + authToken.accessToken());
        response.setHeader(REFRESH_HEADER, BEARER + authToken.refreshToken());

        return ResponseEntity.noContent().build();
    }
}
