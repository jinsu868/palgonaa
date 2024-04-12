package com.palgona.palgona.domain.notification;

import com.palgona.palgona.common.entity.BaseTimeEntity;
import com.palgona.palgona.common.fcm.FCMessage;
import com.palgona.palgona.domain.member.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Notification extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String body;

    @Column(nullable = false)
    private Type type;

    @Column(nullable = false)
    private Long targetId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Builder
    public Notification(String title, String body,Type type, Long targetId, Member member){
        this.title = title;
        this.body = body;
        this.type = type;
        this.targetId = targetId;
        this.member = member;
    }

    public static Notification from(FCMessage message, Member member){
        return Notification.builder()
                .title(message.message().notification().title())
                .body(message.message().notification().body())
                .type(message.message().data().type())
                .targetId(message.message().data().targetId())
                .member(member)
                .build();
    }

    public boolean isOwner(Member member){
        return this.member.equals(member);
    }
}
