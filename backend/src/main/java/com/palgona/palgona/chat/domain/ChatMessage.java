package com.palgona.palgona.chat.domain;

import com.palgona.palgona.common.entity.BaseTimeEntity;
import com.palgona.palgona.member.domain.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessage extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 1024)
    private String message;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private ChatRoom room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private Member receiver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private Member sender;

    public static ChatMessage of(
            String message,
            ChatRoom chatRoom,
            Member sender,
            Member receiver
    ) {
        return new ChatMessage(
                message,
                chatRoom,
                sender,
                receiver
        );
    }
    private ChatMessage(
            String message,
            ChatRoom chatRoom,
            Member receiver,
            Member sender
    ){
        this.message = message;
        this.room = chatRoom;
        this.receiver = receiver;
        this.sender = sender;
    }
}