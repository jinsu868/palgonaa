package com.palgona.palgona.repository.product.querydto;

import com.palgona.palgona.domain.member.Member;
import com.palgona.palgona.domain.product.Product;

import java.time.LocalDateTime;

public record ProductDetailQueryResponse(
        Product product,
        Long ownerId,
        String ownerName,
        String ownerImgUrl,
        int highestBid,
        int bookmarkCount
) {
}
