package com.palgona.palgona.common.error.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SlientNotificationsErrorCode implements ErrorCode {
    NOTIFICATION_ALREADY_SILENCED(HttpStatus.BAD_REQUEST, "S_001", "이미 알림 무시를 추가하였습니다."),
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "S_002", "알림 무시가 존재하지 않습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
