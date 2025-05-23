package com.palgona.palgona.product.dto;

public record ProductWithBidInfo(
        Long productId,
        Long userId,
        int amount
) {
}
