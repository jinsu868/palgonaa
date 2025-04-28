package com.palgona.palgona.auth.application;

import org.springframework.stereotype.Service;

import com.palgona.palgona.auth.domain.RefreshToken;
import com.palgona.palgona.auth.domain.repository.RefreshTokenRepository;
import com.palgona.palgona.auth.util.JwtUtils;
import com.palgona.palgona.common.error.BadRequestException;
import com.palgona.palgona.common.error.ErrorCode;
import com.palgona.palgona.user.domain.User;
import com.palgona.palgona.auth.domain.AuthTokens;
import com.palgona.palgona.auth.dto.request.LoginRequest;
import com.palgona.palgona.user.domain.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;
    private final RefreshTokenRepository refreshTokenRepository;

    public AuthTokens login(LoginRequest loginRequest) {
        User user = userRepository.findByEmail(loginRequest.email())
                .orElseThrow(() -> new BadRequestException(ErrorCode.USER_NOT_FOUND));

        if (!user.getPassword().equals(loginRequest.password())) {
            throw new BadRequestException(ErrorCode.NOT_MATCH_PASSWORD);
        }

        AuthTokens authTokens = jwtUtils.createLoginToken(user.getId().toString());
        RefreshToken refreshToken = new RefreshToken(user.getId(), authTokens.refreshToken());
        refreshTokenRepository.save(refreshToken);
        return authTokens;
    }

    public String reissueAccessToken(String refreshToken, String authHeader) {
        String accessToken = authHeader.split(" ")[1];

        jwtUtils.validateRefreshToken(refreshToken);

        if (jwtUtils.isAccessTokenValid(accessToken)) {
            return accessToken;
        }

        if (jwtUtils.isAccessTokenExpired(accessToken)) {
            RefreshToken foundRefreshToken = refreshTokenRepository.findById(refreshToken)
                    .orElseThrow(() -> new BadRequestException(ErrorCode.INVALID_REFRESH_TOKEN));
            return jwtUtils.reissueAccessToken(foundRefreshToken.getUserId().toString());
        }

        throw new BadRequestException(ErrorCode.FAILED_TO_VALIDATE_TOKEN);
    }

    public void logout(String refreshToken) {
        refreshTokenRepository.deleteById(refreshToken);
    }
}
