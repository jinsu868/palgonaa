package com.palgona.palgona.member.dto.response;

import com.palgona.palgona.member.domain.Member;

public record MemberDetailResponse(
        Long id,
        String nickName,
        int mileage,
        String profileImage
) {
    public static MemberDetailResponse from(Member member) {
        return new MemberDetailResponse(
                member.getId(),
                member.getNickName(),
                member.getMileage(),
                member.getProfileImage()
        );
    }
}
