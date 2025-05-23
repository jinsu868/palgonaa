package com.palgona.palgona.bidding.domain;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
public class BidEvent {

    private final Long id;
    private final Long productId;
    private final Long userId;
    private final int amount;
    private final LocalDateTime createdAt;

    @Builder
    public BidEvent(
            Long id,
            Long productId,
            Long userId,
            int amount
    ) {
        this.id = id;
        this.productId = productId;
        this.userId = userId;
        this.amount = amount;
        this.createdAt = LocalDateTime.now();
    }
}
