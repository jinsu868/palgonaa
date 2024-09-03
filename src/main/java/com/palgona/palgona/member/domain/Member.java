package com.palgona.palgona.member.domain;

import static com.palgona.palgona.common.error.code.MemberErrorCode.INSUFFICIENT_MILEAGE;

import com.palgona.palgona.common.entity.BaseTimeEntity;
import com.palgona.palgona.common.error.exception.BusinessException;
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

    private int mileage;

    @Column(nullable = false, unique = true)
    private String socialId;

    private String profileImage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    private Member(int mileage, Status status, String socialId, Role role) {
        this.mileage = mileage;
        this.status = status;
        this.socialId = socialId;
        this.role = role;
    }

    public static Member of(int mileage, Status status, String socialId, Role role) {
        return new Member(mileage, status, socialId, role);
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

    public void updateMileage(int after){ this.mileage = after; }

    public void signUp() {
        this.role = Role.USER;
    }

    public void useMileage(int usage) throws BusinessException {
        if (mileage < usage) {
            throw new BusinessException(INSUFFICIENT_MILEAGE);
        }

        mileage -= usage;
    }

    public void refundMileage(int price) {
        mileage += price;
    }

    public void receivePayment(int price) {
        mileage += price;
    }
}