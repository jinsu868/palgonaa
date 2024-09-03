package com.palgona.palgona.chat.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ChatType {
    TEXT("TEXT", "문자"),
    IMAGE("IMAGE", "사진");

    private final String key;
    private final String value;
}
