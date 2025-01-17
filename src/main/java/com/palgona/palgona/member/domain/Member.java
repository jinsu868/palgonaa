package com.palgona.palgona.member.domain;

import com.palgona.palgona.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 20, unique = true)
    private String nickName;

    @Column(nullable = false, unique = true)
    private String socialId;

    private String profileImage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    private Member(Status status, String socialId, Role role) {
        this.status = status;
        this.socialId = socialId;
        this.role = role;
    }

    public static Member of(Status status, String socialId, Role role) {
        return new Member(status, socialId, role);
    }

    public boolean isUser() {
        return role == Role.USER;
    }

    public boolean isGuest() {
        return role == Role.GUEST;
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    public void updateNickName(String nickName) {
        this.nickName = nickName;
    }

    public void updateProfileImage(String imageUrl) {
        this.profileImage = imageUrl;
    }

    public void signUp() {
        this.role = Role.USER;
    }
}
