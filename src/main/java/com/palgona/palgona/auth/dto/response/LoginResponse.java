package com.palgona.palgona.auth.dto.response;

import com.palgona.palgona.member.domain.Member;

public record LoginResponse(
        Long id,
        String socialId
) {

    public static LoginResponse from(Member member) {
        return new LoginResponse(
                member.getId(),
                member.getSocialId()
        );
    }
}
