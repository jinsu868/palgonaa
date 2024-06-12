package com.palgona.palgona.dto;

import java.time.LocalDateTime;

public record BookmarkProductsResponse(
        Long id,
        String name,
        LocalDateTime deadline,
        LocalDateTime created_at,
        Integer currentBid,
        Long bookmarkCount,
        String imageUrl
) {
}
