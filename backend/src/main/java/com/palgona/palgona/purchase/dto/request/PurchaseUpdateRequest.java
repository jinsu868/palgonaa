package com.palgona.palgona.purchase.dto.request;

import com.palgona.palgona.purchase.domain.PurchaseState;

public record PurchaseUpdateRequest(
        PurchaseState state
) {
}
