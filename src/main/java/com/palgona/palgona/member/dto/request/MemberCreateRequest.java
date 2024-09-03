package com.palgona.palgona.member.dto.request;

import org.springframework.web.multipart.MultipartFile;

public record MemberCreateRequest(
        String nickName,
        MultipartFile image) {
}
