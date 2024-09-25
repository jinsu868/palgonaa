package com.palgona.palgona.chat.application;

import static com.palgona.palgona.common.error.code.ChatErrorCode.*;
import static com.palgona.palgona.common.error.code.MemberErrorCode.*;

import com.palgona.palgona.chat.dto.response.ChatMessageResponse;
import com.palgona.palgona.chat.infrastructure.RedisSubscriber;
import com.palgona.palgona.common.error.exception.BusinessException;
import com.palgona.palgona.chat.domain.ChatMessage;
import com.palgona.palgona.chat.domain.ChatRoom;
import com.palgona.palgona.member.domain.Member;
import com.palgona.palgona.chat.dto.request.ChatMessageRequest;
import com.palgona.palgona.chat.dto.request.ChatRoomCreateRequest;
import com.palgona.palgona.chat.domain.ChatMessageRepository;
import com.palgona.palgona.chat.domain.ChatRoomRepository;
import com.palgona.palgona.member.domain.MemberRepository;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Service;

import java.util.List;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatService {

    private static final String CHANNEL_NAME_PREFIX = "chat-channel:";
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final MemberRepository memberRepository;
    private final RedisSubscriber redisSubscriber;
    private final RedisMessageListenerContainer redisMessageListenerContainer;
    private final Map<Long, ChannelTopic> topics = new ConcurrentHashMap<>();
    private final RedisTemplate redisTemplate;

    public ChatMessage sendMessage(ChatMessageRequest chatMessageRequest) {
        ChatRoom room = findChatRoom(chatMessageRequest.roomId());
        Member sender = findMember(chatMessageRequest.senderId());
        Member receiver = room.getPartner(sender);

        validateMemberInRoom(room, sender);

        ChannelTopic topic = topics.get(getChannelName(room.getId()));
        if (topic == null) {
            topic = ChannelTopic.of(getChannelName(room.getId()));
            redisMessageListenerContainer.addMessageListener(redisSubscriber, topic);
            topics.put(room.getId(), topic);
        }

        ChatMessage chatMessage = ChatMessage.of(
                chatMessageRequest.content(),
                room,
                sender,
                receiver
        );

        redisTemplate.convertAndSend(getChannelName(room.getId()), chatMessageRequest);
        return chatMessageRepository.save(chatMessage);
    }

    public Long createRoom(
            Member sender,
            ChatRoomCreateRequest request
    ) {
        Member receiver = findMember(request.visitorId());

        validateChatRoomCreate(sender, receiver);

        ChatRoom chatRoom = ChatRoom.of(sender, receiver);
        chatRoomRepository.save(chatRoom);

        return chatRoom.getId();
    }

    public List<ChatMessageResponse> findMessages(
            Member member,
            Long roomId
    ) {
        ChatRoom room = findChatRoom(roomId);

        validateMemberInRoom(room, member);

        return chatMessageRepository.findAllByRoom(room).stream()
                .map(ChatMessageResponse::from)
                .toList();
    }

    @Transactional
    public void leaveChatRoom(
            Long roomId,
            Member member
    ){
        ChatRoom room = findChatRoom(roomId);

        validateMemberInRoom(room, member);

        if (room.isReceiver(member)) {
            room.leaveReceiver();
        } else {
            room.leaveSender();
        }

        chatRoomRepository.save(room);
    }

    private Member findMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(MEMBER_NOT_EXIST));
    }

    private ChatRoom findChatRoom(Long roomId) {
        return chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(CHATROOM_NOT_FOUND));
    }

    private void validateChatRoomCreate(Member sender, Member receiver) {
        if (chatRoomRepository.existsBySenderAndReceiver(sender, receiver)) {
            throw new BusinessException(ALREADY_EXISTS_CHAT_ROOM);
        }
    }

    private void validateMemberInRoom(ChatRoom room, Member sender) {
        if (!room.hasMember(sender)) {
            throw new BusinessException(INVALID_MEMBER);
        }
    }

    private String getChannelName(Long roomId) {
        return CHANNEL_NAME_PREFIX + roomId;
    }
}
