package com.palgona.palgona.repository.product.querydto;

import com.palgona.palgona.domain.member.Member;
import com.palgona.palgona.domain.product.Category;
import com.palgona.palgona.domain.product.Product;
import com.palgona.palgona.domain.product.ProductState;

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
        Integer highestBid,
        Long bookmarkCount,
        Long chatroomCount,
        Boolean isSilent
) {
}
