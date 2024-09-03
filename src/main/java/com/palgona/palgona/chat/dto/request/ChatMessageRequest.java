package com.palgona.palgona.chat.dto.request;

public record ChatMessageRequest(Long senderId, Long receiverId, Long roomId, String message, String imgData) {
}
