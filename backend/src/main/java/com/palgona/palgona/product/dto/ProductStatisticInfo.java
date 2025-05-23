package com.palgona.palgona.product.dto;

public record ProductStatisticInfo(
        Long productId,
        int bookmarkCount,
        double score
) {
    public static ProductStatisticInfo of(Long productId, int bookmarkCount, double score) {
        return new ProductStatisticInfo(productId, bookmarkCount, score);
    }
}
