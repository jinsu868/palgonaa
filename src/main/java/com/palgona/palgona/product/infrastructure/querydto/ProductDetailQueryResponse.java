package com.palgona.palgona.product.infrastructure.querydto;

import com.palgona.palgona.product.domain.Category;
import com.palgona.palgona.product.domain.ProductState;

import java.time.LocalDateTime;

public record ProductDetailQueryResponse(
        Long productId,
        String productName,
        String content,
        Category category,
        ProductState productState,
        LocalDateTime deadline,
        LocalDateTime created_at,
        Long ownerId,
        String ownerName,
        String ownerImgUrl,
        Integer highestBid
) {
}
