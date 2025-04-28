package com.palgona.palgona.mileage.domain;

import static com.palgona.palgona.common.error.code.MileageErrorCode.INVALID_CHARGE_AMOUNT;
import static com.palgona.palgona.common.error.code.MileageErrorCode.INVALID_MILEAGE_TRANSACTION;

import com.palgona.palgona.common.error.exception.BusinessException;
import com.palgona.palgona.member.domain.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Mileage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(nullable = false)
    private int balance;

    public static Mileage from(Member member) {
        return new Mileage(member);
    }

    private Mileage(Member member) {
        this.member = member;
        this.balance = 0;
    }

    public void use(Integer amount) {
        if (balance < amount) {
            throw new BusinessException(INVALID_MILEAGE_TRANSACTION);
        }

        balance -= amount;
    }

    public void charge(Integer amount) {
        if (amount < 0) {
            throw new BusinessException(INVALID_CHARGE_AMOUNT);
        }

        balance += amount;
    }

    public void refund(Integer amount) {
        balance += amount;
    }
}
