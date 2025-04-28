package com.palgona.palgona.chat.dto.response;

import com.palgona.palgona.chat.domain.ChatRoom;

public record ChatRoomResponse(
        Long id,
        Long senderId,
        Long receiverId,
        boolean isLeaveSender,
        boolean isLeaveReceiver
) {
    public static ChatRoomResponse from(ChatRoom chatRoom) {
        return new ChatRoomResponse(chatRoom.getId(),
                chatRoom.getSender().getId(),
                chatRoom.getReceiver().getId(),
                chatRoom.isLeaveSender(),
                chatRoom.isLeaveReceiver());
    }
}
