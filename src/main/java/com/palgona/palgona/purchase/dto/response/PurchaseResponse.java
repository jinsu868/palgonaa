package com.palgona.palgona.purchase.dto.response;

import com.palgona.palgona.product.domain.Category;
import com.palgona.palgona.purchase.domain.PurchaseState;
import com.palgona.palgona.purchase.infrastructure.queryDto.PurchaseQueryResponse;

public record PurchaseResponse(
        Long id,
        Long productId,
        int price,
        Category category,
        PurchaseState state,
        String reason,
        String productName,
        String imageUrl
) {

    public static PurchaseResponse of(PurchaseQueryResponse response, String imageUrl) {
        return new PurchaseResponse(
                response.id(),
                response.productId(),
                response.price(),
                response.category(),
                response.state(),
                response.reason(),
                response.productName(),
                imageUrl
        );
    }
}
