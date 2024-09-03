package com.palgona.palgona.member.dto.response;

import com.palgona.palgona.member.domain.Member;

public record MemberResponse(
        Long id,
        String nickName,
        String profileImage
) {

    public static MemberResponse from(Member member) {
        return new MemberResponse(
                member.getId(),
                member.getNickName(),
                member.getProfileImage()
        );
    }
}
