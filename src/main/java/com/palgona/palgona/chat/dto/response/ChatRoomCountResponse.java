package com.palgona.palgona.chat.dto.response;

public record ChatRoomCountResponse(
        Long id,
        Long senderId,
        Long receiverId,
        Long unreadMessageCount
) {
}
