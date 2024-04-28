package com.palgona.palgona.dto.purchase;

import com.palgona.palgona.domain.product.Category;
import com.palgona.palgona.domain.purchase.PurchaseState;
import com.palgona.palgona.repository.purchase.queryDto.PurchaseQueryResponse;

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
