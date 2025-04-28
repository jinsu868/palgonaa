package com.palgona.palgona.member.dto.response;

import com.palgona.palgona.member.domain.Member;

public record MemberDetailResponse(
        Long id,
        String nickName,
        int mileage,
        String profileImage
) {
    public static MemberDetailResponse of(Member member, int balance) {
        return new MemberDetailResponse(
                member.getId(),
                member.getNickName(),
                balance,
                member.getProfileImage()
        );
    }
}
