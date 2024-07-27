package com.palgona.palgona.domain.chat;

import com.palgona.palgona.common.entity.BaseTimeEntity;
import com.palgona.palgona.domain.member.Member;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "chat_room")
public class ChatRoom extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @ColumnDefault("false")
    @Column(columnDefinition = "TINYINT(1)")
    private boolean isLeaveSender;

    @Setter
    @ColumnDefault("false")
    @Column(columnDefinition = "TINYINT(1)")
    private boolean isLeaveReceiver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private Member sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private Member receiver;

    @OneToMany(fetch = FetchType.LAZY)
    private List<ChatMessage> chatMessages;

    @Builder
    ChatRoom(Member sender, Member receiver){
        this.sender = sender;
        this.receiver = receiver;
        this.isLeaveSender = false;
        this.isLeaveReceiver = false;
    }

    public boolean hasMember(Member member) {
        return member.equals(sender) || member.equals(receiver);
    }
}

