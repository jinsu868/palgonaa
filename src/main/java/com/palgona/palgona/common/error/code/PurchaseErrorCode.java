package com.palgona.palgona.common.error.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum PurchaseErrorCode implements ErrorCode{

    PURCHASE_NOT_FOUND(HttpStatus.OK, "PC_001", "구매 기록을 찾을 수 없습니다."),
    INSUFFICIENT_PERMISSION(HttpStatus.FORBIDDEN, "PC_002", "구매 기록에 대한 권한이 없습니다."),
    PURCHASE_EXPIRED(HttpStatus.BAD_REQUEST, "PC_003", "이미 구매 확정 기간이 지났습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
