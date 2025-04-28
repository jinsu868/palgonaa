package com.palgona.palgona.user.dto.request;

public record UserUpdateRequest(
        String email,
        String nickname,
        String password
) {
}
