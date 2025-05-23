package com.palgona.palgona.purchase.domain;

import com.palgona.palgona.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Purchase extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "purchase_id")
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "amount", nullable = false)
    private int amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    private PurchaseState state;

    @Builder
    public Purchase(Long id, Long productId, Long userId, int amount) {
        this.id = id;
        this.productId = productId;
        this.userId = userId;
        this.amount = amount;
        this.state = PurchaseState.ONGOING;
    }

    public boolean isOwner(Long userId) {
        return userId.equals(userId);
    }

    public boolean isOngoing() {
        return state == PurchaseState.ONGOING;
    }

    public void cancel() {
        state = PurchaseState.CANCEL;
    }

    public void complete() {
        state = PurchaseState.COMPLETED;
    }
}
