package com.palgona.palgona.product.infrastructure.querydto;

import java.time.LocalDateTime;

public record ProductQueryResponse(
        Long id,
        String name,
        int currentBid,
        LocalDateTime deadline,
        LocalDateTime createdAt
) {
}
