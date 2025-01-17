package com.palgona.palgona.product.domain;

import lombok.Getter;

@Getter
public enum ProductState {
    ON_SALE("ON_SALE", "판매"),
    SOLD_OUT("SOLD_OUT", "판매 완료"),
    DELETED("DELETED", "삭제"),
    EXPIRED("EXPIRED", "만료");

    private final String key;
    private final String value;

    ProductState(String key, String value) {
        this.key = key;
        this.value = value;
    }
}
