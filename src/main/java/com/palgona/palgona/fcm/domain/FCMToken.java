package com.palgona.palgona.fcm.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FCMToken {
    @Id
    private String token;

    @Column(nullable = false, unique = true)
    private String socialId;

    @Builder
    public FCMToken(String token, String socialId) {
        this.token = token;
        this.socialId = socialId;
    }
}
