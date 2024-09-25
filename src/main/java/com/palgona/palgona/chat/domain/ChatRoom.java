package com.palgona.palgona.chat.domain;

import com.palgona.palgona.common.entity.BaseTimeEntity;
import com.palgona.palgona.member.domain.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Getter
@NoArgsConstructor
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

    public static ChatRoom of(
            Member sender,
            Member receiver
    ) {
        return new ChatRoom(
                sender,
                receiver
        );
    }

    private ChatRoom(Member sender, Member receiver){
        this.sender = sender;
        this.receiver = receiver;
        this.isLeaveSender = false;
        this.isLeaveReceiver = false;
    }

    public boolean hasMember(Member member) {
        return (member.getId() == sender.getId() && !isLeaveSender) ||
                (member.getId() == receiver.getId() && !isLeaveReceiver);
    }

    public Member getPartner(Member sender) {
        if (sender.getId() == this.sender.getId()) {
            return receiver;
        }
        return sender;
    }

    public boolean isReceiver(Member member) {
        if (member.getId() == receiver.getId()) {
            return true;
        }
        return false;
    }

    public void leaveReceiver() {
        isLeaveReceiver = true;
    }

    public void leaveSender() {
        isLeaveSender = true;
    }
}

