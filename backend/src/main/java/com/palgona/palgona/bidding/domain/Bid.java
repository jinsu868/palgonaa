package com.palgona.palgona.bidding.domain;

import com.palgona.palgona.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
public class Bid extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bid_id")
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "state", nullable = false)
    private BidState state;

    @Column(name = "amount", nullable = false)
    private int amount;

    @Builder
    public Bid(
            Long id,
            Long productId,
            Long userId,
            BidState state,
            int amount
    ) {
        this.id = id;
        this.productId = productId;
        this.userId = userId;
        this.state = state;
        this.amount = amount;
    }
}
