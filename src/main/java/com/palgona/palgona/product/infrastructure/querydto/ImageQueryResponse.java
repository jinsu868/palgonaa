package com.palgona.palgona.product.infrastructure.querydto;

public record ImageQueryResponse(
        Long productId,
        Long imageId,
        String imageUrl
) {
}
