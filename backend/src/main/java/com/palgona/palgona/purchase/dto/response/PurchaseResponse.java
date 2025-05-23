package com.palgona.palgona.purchase.dto.response;

import com.palgona.palgona.purchase.domain.PurchaseState;

public record PurchaseResponse(
        Long id,
        Long productId,
        int amount,
        String productName,
        PurchaseState state
) {
}
