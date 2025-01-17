package com.palgona.palgona.mileage.domain;

public enum MileageState {
    CHARGE("CHARGE", "충전"),
    USE("USE", "사용"),
    SALE("SALE", "판매");

    private final String key;
    private final String value;

    MileageState(String key, String value) {
        this.key = key;
        this.value = value;
    }
}
