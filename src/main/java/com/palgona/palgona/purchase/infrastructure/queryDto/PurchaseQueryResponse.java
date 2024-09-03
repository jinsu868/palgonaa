package com.palgona.palgona.purchase.infrastructure.queryDto;

import com.palgona.palgona.product.domain.Category;
import com.palgona.palgona.purchase.domain.PurchaseState;

public record PurchaseQueryResponse(
        Long id,
        Long productId,
        int price,
        Category category,
        PurchaseState state,
        String reason,
        String productName
) {
}
