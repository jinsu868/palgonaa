package com.palgona.palgona.common.error.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum NotificationErrorCode implements ErrorCode {
    INVALID_ID(HttpStatus.BAD_REQUEST, "N_001", "해당하는 알림이 존재하지 않습니다."),
    INSUFFICIENT_PERMISSION(HttpStatus.FORBIDDEN, "N_002", "해당 알림에 대한 권한이 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}