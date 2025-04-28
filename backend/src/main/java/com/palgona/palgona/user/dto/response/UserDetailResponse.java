package com.palgona.palgona.user.dto.response;

import com.palgona.palgona.user.domain.User;

public record UserDetailResponse(
        Long id,
        String nickname,
        String email
) {
    public static UserDetailResponse from(User user) {
        return new UserDetailResponse(
                user.getId(),
                user.getNickname(),
                user.getEmail()
        );
    }
}
