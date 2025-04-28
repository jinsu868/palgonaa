package com.palgona.palgona.chat.presentation;

import com.palgona.palgona.chat.dto.request.ChatMessageRequest;
import com.palgona.palgona.chat.dto.request.ChatRoomCreateRequest;
import com.palgona.palgona.chat.dto.response.ChatMessageResponse;
import com.palgona.palgona.chat.dto.response.ChatRoomResponse;
import com.palgona.palgona.common.dto.CustomMemberDetails;
import com.palgona.palgona.chat.domain.ChatMessage;
import com.palgona.palgona.chat.application.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/v1/chats")
public class ChatController {
    private final ChatService chatService;
    private final RedisTemplate<String, Object> redisChatTemplate;

    @MessageMapping("/sendMessage")
    @Operation(summary = "채팅 발송 api",
            description = "socket통신으로 체팅을 받아 발송한다.")
    public void sendMessage(@Payload ChatMessageRequest message) {
        ChatMessage savedMessage = chatService.sendMessage(message);
        ChatMessageResponse messageResponse = ChatMessageResponse.from(savedMessage);

        redisChatTemplate.convertAndSend("chatroom", messageResponse);
    }

    @PostMapping
    @Operation(summary = "채팅방 생성 api",
            description = "판매자, 구매자 사이에 채팅방을 생성한다.")
    public ResponseEntity<ChatRoomResponse> createChatRoom(
            @RequestBody ChatRoomCreateRequest request,
            @AuthenticationPrincipal CustomMemberDetails loginMember
    ) {
        Long chatRoomId = chatService.createRoom(loginMember.getMember(), request);

        return ResponseEntity.created(URI.create("api/v1/chats/" + chatRoomId))
                .build();
    }

    @GetMapping("/{roomId}")
    @Operation(summary = "채팅방의 채팅 목록 조회 api",
            description = "현재 채팅방의 채팅 목록을 불러온다.")
    public ResponseEntity<List<ChatMessageResponse>> readRoomChat(
            @AuthenticationPrincipal CustomMemberDetails loginMember,
            @PathVariable Long roomId
    ) {
        List<ChatMessageResponse> response = chatService.findMessages(loginMember.getMember(), roomId);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{roomId}/exit")
    @Operation(summary = "채팅방 나가기 api",
            description = "현재 채팅방을 나간다.")
    public ResponseEntity<ChatRoomResponse> exitChatRoom(
            @AuthenticationPrincipal CustomMemberDetails loginMember,
            @PathVariable Long roomId
    ) {
        chatService.leaveChatRoom(roomId, loginMember.getMember());

        return ResponseEntity.ok().build();
    }
}
