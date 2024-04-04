package com.palgona.palgona.domain.purchase;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum PurchaseState {
    CONFIRM("CONFIRM", "확정"),
    WAIT("WAIT", "대기"),
    CANCEL("CANCEL", "취소");

    private final String key;
    private final String value;
}
