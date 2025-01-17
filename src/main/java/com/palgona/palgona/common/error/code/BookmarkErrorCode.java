package com.palgona.palgona.common.error.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
@Getter
@AllArgsConstructor
public enum BookmarkErrorCode implements ErrorCode {
    BOOKMARK_EXISTS(HttpStatus.BAD_REQUEST, "BM_001", "이미 추가된 상품입니다."),
    BOOKMARK_NOT_EXISTS(HttpStatus.BAD_REQUEST, "BM_002", "찜이 존재하지 않습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
