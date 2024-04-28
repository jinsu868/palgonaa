package com.palgona.palgona.repository.purchase.queryDto;

import com.palgona.palgona.domain.product.Category;
import com.palgona.palgona.domain.purchase.PurchaseState;

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
