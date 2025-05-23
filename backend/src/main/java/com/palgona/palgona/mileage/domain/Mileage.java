package com.palgona.palgona.mileage.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import com.palgona.palgona.common.error.BadRequestException;
import com.palgona.palgona.common.error.ErrorCode;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Mileage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mileage_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "balance", nullable = false)
    private int balance;

    @Builder
    public Mileage(
            Long id,
            Long userId,
            int balance
    ) {
        this.id = id;
        this.userId = userId;
        this.balance = balance;
    }

    public boolean isPayable(int amount) {
        return balance >= amount;
    }

    public void use(int amount) {
        if (balance < amount) {
            throw new BadRequestException(ErrorCode.NOT_ENOUGH_BALANCE);
        }

        balance -= amount;
    }

    public void charge(int amount) {
        if (amount < 0) {
            throw new BadRequestException(ErrorCode.INVALID_CHARGE_BALANCE);
        }

        balance += amount;
    }

    public void refund(Integer amount) {
        balance += amount;
    }
}
