package com.palgona.palgona.chat.dto.request;

public record ChatMessageRequest(
        Long senderId,
        Long roomId,
        String content
) {
}
