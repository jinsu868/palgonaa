package com.palgona.palgona.domain.notification;


import lombok.Getter;

import java.util.Arrays;

@Getter
public enum Type {
    BIDDING_UPDATE("BIDDING_UPDATE", "입찰 업데이트"),
    PURCHASE_COMPLETE("PURCHASE_COMPLETE", "낙찰 완료"),
    CHAT_MESSAGE("CHAT_MESSAGE", "채팅"),
    BOOKMARK("BOOKMARK", "찜 추가");


    private final String key;
    private final String value;

    public static Type from(String name) {
        return Arrays.stream(values())
                .filter(category -> category.getKey().equals(name))
                .findAny()
                .orElse(null);
    }

    Type(String key, String value) {
        this.key = key;
        this.value = value;
    }
}
