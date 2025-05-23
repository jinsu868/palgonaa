package com.palgona.palgona.product.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record ProductResponse(
        Long id,
        String name,
        int currentBid,
        LocalDateTime deadline,
        String imageUrl,
        String buyerName,
        int bookmarkCount,
        double score
) {
}
