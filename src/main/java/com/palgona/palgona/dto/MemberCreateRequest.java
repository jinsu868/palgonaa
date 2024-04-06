package com.palgona.palgona.dto;

import org.springframework.web.multipart.MultipartFile;

public record MemberCreateRequest(
        String nickName,
        MultipartFile image) {
}
