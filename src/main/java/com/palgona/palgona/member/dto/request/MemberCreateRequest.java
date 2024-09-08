package com.palgona.palgona.member.dto.request;

import org.springframework.web.multipart.MultipartFile;

public record MemberCreateRequest(
        String nickName,
        MultipartFile image
) {

    public static MemberCreateRequest of(
            MemberCreateRequestWithoutImage request,
            MultipartFile file
    ) {
        return new MemberCreateRequest(
                request.nickName(),
                file
        );
    }

}
