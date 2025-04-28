package com.palgona.palgona.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "nickname", length = 32, unique = true)
    private String nickname;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "profile_image", length = 2048)
    private String profileImage;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private UserStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @Builder
    public User(
            String nickname,
            String password,
            String email,
            String profileImage,
            Long id
    ) {
        this.id = id;
        this.nickname = nickname;
        this.password = password;
        this.email = email;
        this.profileImage = profileImage;
    }

    public void update(
            String email,
            String nickname,
            String profileImage,
            String password
    ) {
        this.email = email;
        this.nickname = nickname;
        this.profileImage = profileImage;
        this.password = password;
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }
}
