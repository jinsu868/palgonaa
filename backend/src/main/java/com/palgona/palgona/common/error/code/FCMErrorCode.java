package com.palgona.palgona.common.error.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum FCMErrorCode implements ErrorCode {
    FAILED_TO_GENERATE_FCM_JSON(HttpStatus.BAD_REQUEST, "F_001", "FCM JSON 메시지 생성에 실패하였습니다."),
    FAILED_TO_GET_ACCESS_TOKEN(HttpStatus.BAD_REQUEST, "F_002", "Access token 가져오기에 실패하였습니다."),
    FAILED_TO_SEND_FCM(HttpStatus.BAD_REQUEST, "F_003", "FCM 전송에 실패하였습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
