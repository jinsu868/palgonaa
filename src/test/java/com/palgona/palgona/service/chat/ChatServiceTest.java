package com.palgona.palgona.service.chat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.palgona.palgona.common.error.code.ChatErrorCode;
import com.palgona.palgona.common.error.exception.BusinessException;
import com.palgona.palgona.chat.domain.ChatMessage;
import com.palgona.palgona.chat.domain.ChatReadStatus;
import com.palgona.palgona.chat.domain.ChatRoom;
import com.palgona.palgona.chat.domain.ChatType;
import com.palgona.palgona.member.domain.Member;
import com.palgona.palgona.member.domain.Role;
import com.palgona.palgona.member.domain.Status;
import com.palgona.palgona.chat.dto.request.ChatMessageRequest;
import com.palgona.palgona.chat.dto.request.ChatRoomCreateRequest;
import com.palgona.palgona.chat.dto.request.ReadMessageRequest;
import com.palgona.palgona.chat.domain.ChatMessageRepository;
import com.palgona.palgona.chat.domain.ChatReadStatusRepository;
import com.palgona.palgona.chat.domain.ChatRoomRepository;
import com.palgona.palgona.member.domain.MemberRepository;
import com.palgona.palgona.chat.application.ChatService;
import com.palgona.palgona.image.domain.S3Client;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;

class ChatServiceTest {
    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private ChatReadStatusRepository chatReadStatusRepository;

    @InjectMocks
    private ChatService chatService;

    @Mock
    private S3Client s3Client;


    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("채팅방에 없는 유저로 채팅방에 메시지를 보내 실패한다.")
    public void testSendMessageWithInvalidMember() {
        // given
        int mileage = 1000;
        Status status = Status.ACTIVE;
        String socialId = "1111";
        Role role = Role.USER;

        ChatMessageRequest messageDto = new ChatMessageRequest(1L, 2L, 3L, "Hello", null);
        Member sender = Member.of(mileage, status, socialId, role);
        Member receiver = Member.of(mileage, status, socialId, role);
        Member another = Member.of(mileage, status, socialId, role);
        ChatRoom room = ChatRoom.builder().sender(sender).receiver(receiver).build();

        given(memberRepository.findById(1L)).willReturn(Optional.of(sender));
        given(memberRepository.findById(2L)).willReturn(Optional.of(another));
        given(chatRoomRepository.findById(3L)).willReturn(Optional.of(room));

        // when
        BusinessException exception = assertThrows(BusinessException.class, () -> chatService.sendMessage(messageDto));

        // then
        assertEquals(ChatErrorCode.INVALID_MEMBER, exception.getErrorCode());
    }


    @Test
    @DisplayName("메시지를 보내는데 성공한다.")
    public void testSendMessageSuccess() {
        // given
        int mileage = 1000;
        Status status = Status.ACTIVE;
        String socialId = "1111";
        Role role = Role.USER;

        Member sender = Member.of(mileage, status, socialId, role);
        Member receiver = Member.of(mileage, status, socialId, role);
        ChatRoom room = ChatRoom.builder().sender(sender).receiver(receiver).build();
        ChatMessageRequest messageDto = new ChatMessageRequest(1L, 2L, 3L, "Hello", null);
        ChatMessage message = ChatMessage.builder()
                .sender(sender)
                .receiver(receiver)
                .message(messageDto.message())
                .room(room)
                .type(ChatType.TEXT)
                .build();

        given(memberRepository.findById(1L)).willReturn(Optional.of(sender));
        given(memberRepository.findById(2L)).willReturn(Optional.of(receiver));
        given(chatRoomRepository.findById(3L)).willReturn(Optional.of(room));
        given(chatMessageRepository.save(any())).willReturn(message);

        // when
        ChatMessage result = chatService.sendMessage(messageDto);

        // then
        assertEquals("Hello", result.getMessage());
    }

    @Test
    @DisplayName("채팅방을 만드는데 성공한다.")
    public void testCreateRoomSuccess() {
        // given
        int mileage = 1000;
        Status status = Status.ACTIVE;
        String socialId = "1111";
        Role role = Role.USER;

        Member sender = Member.of(mileage, status, socialId, role);
        Member receiver = Member.of(mileage, status, socialId, role);
        ChatRoom room = ChatRoom.builder().sender(sender).receiver(receiver).build();
        ChatRoomCreateRequest request = new ChatRoomCreateRequest(1L);

        given(memberRepository.findById(1L)).willReturn(Optional.of(receiver));
        given(chatRoomRepository.findBySenderAndReceiver(sender, receiver)).willReturn(Optional.empty());
        given(chatRoomRepository.save(any())).willReturn(room);

        // when
        ChatRoom createdRoom = chatService.createRoom(sender, request);

        // then
        assertNotNull(createdRoom);
        assertEquals(sender, createdRoom.getSender());
        assertEquals(receiver, createdRoom.getReceiver());
        verify(chatReadStatusRepository, times(1)).saveAll(any());
    }

    @Test
    @DisplayName("현재 채팅방의 읽은 메시지 커서를 최신화한다.")
    public void testReadMessageSuccess() {
        // given
        int mileage = 1000;
        Status status = Status.ACTIVE;
        String socialId = "1111";
        Role role = Role.USER;
        Member sender = Member.of(mileage, status, socialId, role);
        Member receiver = Member.of(mileage, status, socialId, role);
        ChatRoom room = ChatRoom.builder().sender(sender).receiver(receiver).build();
        ChatReadStatus chatReadStatus = ChatReadStatus.builder().room(room).member(sender).build();
        Member member = mock(Member.class);
        Long messageId = 100L;
        ReadMessageRequest request = new ReadMessageRequest(messageId);
        ChatMessage message = mock(ChatMessage.class);

        given(chatMessageRepository.findById(messageId)).willReturn(Optional.of(message));
        given(chatReadStatusRepository.findByMemberAndRoom(sender, room)).willReturn(chatReadStatus);
        given(message.getId()).willReturn(messageId);
        given(message.getRoom()).willReturn(room);
        given(message.getReceiver()).willReturn(receiver);
        given(message.getSender()).willReturn(sender);
        given(member.getId()).willReturn(1L);
        given(memberRepository.findById(1L)).willReturn(Optional.of(sender));

        // when
        chatService.readMessage(member, request);

        // then
        verify(chatReadStatusRepository, times(1)).save(chatReadStatus);
        assertEquals(messageId, chatReadStatus.getMessageCursor());
    }

    @Test
    @DisplayName("방별로 메시지를 불러온다.")
    void getMessageByRoom() {
        // given
        int mileage = 1000;
        Status status = Status.ACTIVE;
        String socialId = "1111";
        Role role = Role.USER;
        Member sender = Member.of(mileage, status, socialId, role);
        Member receiver = Member.of(mileage, status, socialId, role);
        ChatRoom room = ChatRoom.builder().sender(sender).receiver(receiver).build();
        Long roomId = 1L;

        ChatMessage message = ChatMessage.builder()
                .sender(sender)
                .receiver(receiver)
                .message("mock")
                .room(room)
                .type(ChatType.TEXT)
                .build();
        Member member = mock(Member.class);

        given(member.getId()).willReturn(1L);
        given(memberRepository.findById(1L)).willReturn(Optional.of(sender));
        given(memberRepository.findById(2L)).willReturn(Optional.of(receiver));
        given(chatRoomRepository.findById(roomId)).willReturn(Optional.ofNullable(room));
        given(chatMessageRepository.findAllByRoom(room)).willReturn(Collections.singletonList(message));

        // when
        List<ChatMessage> chatMessages = chatService.getMessageByRoom(member, roomId);

        // then
        assertEquals(chatMessages, Collections.singletonList(message));
    }

    @Test
    @DisplayName("읽은 상태가 없는 메시지가 올 경우 안읽은 메시지 조회에 실패한다.")
    public void testGetUnreadMessagesByRoom_UnValidChatReadStatus() {
        // given
        int mileage = 1000;
        Status status = Status.ACTIVE;
        String socialId = "1111";
        Role role = Role.USER;
        Member sender = Member.of(mileage, status, socialId, role);
        Member receiver = Member.of(mileage, status, socialId, role);
        ChatRoom room = ChatRoom.builder().sender(sender).receiver(receiver).build();
        Long roomId = 1L;
        ChatReadStatus chatReadStatus = null;

        when(chatRoomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(chatReadStatusRepository.findByMemberAndRoom(sender, room)).thenReturn(chatReadStatus);

        // when
        BusinessException exception = assertThrows(BusinessException.class,
                () -> chatService.getUnreadMessagesByRoom(sender, roomId));

        // then
        assertEquals(ChatErrorCode.READ_STATUS_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("정상적인 메시지일 경우 안읽은 메시지 조회에 성공한다.")
    public void testGetUnreadMessagesByRoom() {
        // Arrange
        int mileage = 1000;
        Status status = Status.ACTIVE;
        String socialId = "1111";
        Role role = Role.USER;
        Member sender = Member.of(mileage, status, socialId, role);
        Member receiver = Member.of(mileage, status, socialId, role);
        ChatRoom room = ChatRoom.builder().sender(sender).receiver(receiver).build();
        Long roomId = 1L;
        Long messageCursor = 50L;
        ChatReadStatus chatReadStatus = ChatReadStatus.builder().build();
        chatReadStatus.updateCursor(messageCursor);
        List<ChatMessage> unreadMessages = new ArrayList<>();
        unreadMessages.add(mock(ChatMessage.class));
        unreadMessages.add(mock(ChatMessage.class));

        // 가짜 읽지 않은 메시지 생성
        when(chatRoomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(chatReadStatusRepository.findByMemberAndRoom(sender, room)).thenReturn(chatReadStatus);
        when(chatMessageRepository.findMessagesAfterCursor(roomId, messageCursor)).thenReturn(unreadMessages);

        // Act
        List<ChatMessage> chatMessages = chatService.getUnreadMessagesByRoom(sender, roomId);

        // Assert
        assertNotNull(chatMessages);
        assertEquals(unreadMessages, chatMessages);
        verify(chatReadStatusRepository, times(1)).save(chatReadStatus);
    }

    @Test
    @DisplayName("존재하지 않는 메시지를 읽으려고 하는 경우 예외가 발생한다.")
    public void readMessage_MessageNotFound_ShouldThrowException() {
        // given
        int mileage = 1000;
        Status status = Status.ACTIVE;
        String socialId = "1111";
        Role role = Role.USER;
        Member sender = Member.of(mileage, status, socialId, role);
        Long messageId = 100L;
        ReadMessageRequest request = new ReadMessageRequest(messageId);
        Member member = mock(Member.class);

        given(chatMessageRepository.findById(messageId)).willReturn(Optional.empty());
        given(member.getId()).willReturn(1L);
        given(memberRepository.findById(1L)).willReturn(Optional.of(sender));

        // when
        BusinessException exception = assertThrows(BusinessException.class,
                () -> chatService.readMessage(member, request));

        // then
        assertEquals(ChatErrorCode.MESSAGE_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("유효한 요청에 대해 파일을 업로드하는 경우.")
    public void uploadChatFile_ValidRequest_ShouldUploadFile() {
        // given
        int mileage = 1000;
        Status status = Status.ACTIVE;
        String socialId = "1111";
        Role role = Role.USER;
        Member sender = Member.of(mileage, status, socialId, role);
        Member receiver = Member.of(mileage, status, socialId, role);
        ChatRoom room = ChatRoom.builder().sender(sender).receiver(receiver).build();
        MultipartFile file = mock(MultipartFile.class);
        String fileUrl = "https://s3.amazonaws.com/bucket/file.png";
        ChatMessage message = ChatMessage.builder()
                .sender(sender)
                .receiver(receiver)
                .room(room)
                .message(fileUrl)
                .type(ChatType.IMAGE)
                .build();
        ChatMessageRequest messageDto = new ChatMessageRequest(1L, 2L, 3L, "Hello", null);

        given(memberRepository.findById(1L)).willReturn(Optional.of(sender));
        given(memberRepository.findById(2L)).willReturn(Optional.of(receiver));
        given(chatRoomRepository.findById(3L)).willReturn(Optional.of(room));
        given(s3Client.upload(file)).willReturn(fileUrl);
        given(chatMessageRepository.save(any(ChatMessage.class))).willReturn(message);

        // when
        ChatMessage result = chatService.uploadChatFile(messageDto, file);

        // then
        assertEquals(fileUrl, result.getMessage());
        verify(chatMessageRepository, times(1)).save(any(ChatMessage.class));
    }

    @Test
    @DisplayName("채팅방에서 나가는데 성공한다.")
    public void testExitChatRoomSuccess() {
        // given
        int mileage = 1000;
        Status status = Status.ACTIVE;
        String socialId = "1111";
        Role role = Role.USER;

        Member sender = Member.of(mileage, status, socialId, role);
        Member receiver = Member.of(mileage, status, socialId, role);
        ChatRoom room = ChatRoom.builder().sender(sender).receiver(receiver).build();
        Long roomId = 1L;

        given(chatRoomRepository.findById(roomId)).willReturn(Optional.of(room));
        given(chatRoomRepository.save(room)).willReturn(room);

        // when (sender가 나갈 때)
        ChatRoom result = chatService.exitChatRoom(roomId, sender);

        // then
        assertTrue(result.isLeaveSender());
        verify(chatRoomRepository, times(1)).save(room);  // once when setting leave status, once in the return statement

        // when (receiver가 나갈 때)
        result = chatService.exitChatRoom(roomId, receiver);

        // then
        assertTrue(result.isLeaveReceiver());
        verify(chatRoomRepository, times(2)).save(room);  // once when setting leave status, once in the return statement
    }

    @Test
    @DisplayName("채팅방에 없는 유저가 나가려고 할 때 예외를 발생시킨다.")
    public void testExitChatRoomInvalidMember() {
        // given
        int mileage = 1000;
        Status status = Status.ACTIVE;
        String socialId = "1111";
        Role role = Role.USER;

        Member sender = Member.of(mileage, status, socialId, role);
        Member receiver = Member.of(mileage, status, socialId, role);
        Member another = Member.of(mileage, status, socialId, role);
        ChatRoom room = ChatRoom.builder().sender(sender).receiver(receiver).build();
        Long roomId = 1L;

        given(chatRoomRepository.findById(roomId)).willReturn(Optional.of(room));

        // when
        BusinessException exception = assertThrows(BusinessException.class, () -> chatService.exitChatRoom(roomId, another));

        // then
        assertEquals(ChatErrorCode.INVALID_MEMBER, exception.getErrorCode());
        verify(chatRoomRepository, never()).save(any());
    }

}
