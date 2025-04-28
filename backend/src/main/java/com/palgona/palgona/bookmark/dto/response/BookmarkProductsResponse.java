package com.palgona.palgona.bookmark.dto.response;

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
