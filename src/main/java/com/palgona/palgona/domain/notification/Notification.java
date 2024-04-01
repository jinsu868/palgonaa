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
    private String targetUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Builder
    public Notification(String title, String body, String targetUrl, Member member){
        this.title = title;
        this.body = body;
        this.targetUrl = targetUrl;
        this.member = member;
    }

    public static Notification from(FCMessage message, Member member){
        return Notification.builder()
                .title(message.message().notification().title())
                .body(message.message().notification().body())
                .targetUrl(message.message().data().url())
                .member(member)
                .build();
    }

    public boolean isOwner(Member member){
        return this.member.equals(member);
    }
}
