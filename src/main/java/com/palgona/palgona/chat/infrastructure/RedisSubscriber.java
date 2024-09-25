package com.palgona.palgona.chat.infrastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.palgona.palgona.chat.dto.request.ChatMessageRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisSubscriber implements MessageListener {

    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final SimpMessageSendingOperations sendingOperations;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String receivedMessage = redisTemplate.getStringSerializer().deserialize(message.getBody());
        ChatMessageRequest request;

        try {
            request = objectMapper.readValue(receivedMessage, ChatMessageRequest.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        sendingOperations.convertAndSend("/queue/chat-rooms/" + request.roomId(), request);
    }
}
