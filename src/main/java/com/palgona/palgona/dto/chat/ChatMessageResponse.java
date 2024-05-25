package com.palgona.palgona.dto.chat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.palgona.palgona.domain.chat.ChatMessage;
import com.palgona.palgona.domain.chat.ChatType;
import java.time.LocalDateTime;

public record ChatMessageResponse(Long id,
                                  Long senderId,
                                  Long receiverId,
                                  String message,
                                  Long roomId,
                                  ChatType chatType,
                                  @JsonSerialize(using = LocalDateTimeSerializer.class) @JsonDeserialize(using = LocalDateTimeDeserializer.class) @JsonFormat(shape = JsonFormat.Shape.STRING,
                                          pattern = "yyyy-MM-dd HH:mm") LocalDateTime createdAt,
                                  @JsonSerialize(using = LocalDateTimeSerializer.class) @JsonDeserialize(using = LocalDateTimeDeserializer.class) @JsonFormat(shape = JsonFormat.Shape.STRING,
                                          pattern = "yyyy-MM-dd HH:mm") LocalDateTime updatedAt) {
    public static ChatMessageResponse from(ChatMessage message) {
        return new ChatMessageResponse(message.getId(),
                message.getSender().getId(),
                message.getReceiver().getId(),
                message.getMessage(),
                message.getRoom().getId(),
                message.getType(),
                message.getCreatedAt(),
                message.getUpdatedAt());
    }
}
