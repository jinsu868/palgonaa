package com.palgona.palgona.product.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.palgona.palgona.product.domain.ProductCategory;
import com.palgona.palgona.product.domain.ProductState;

public record ProductDetailResponse(
        Long productId,
        String productName,
        String content,
        ProductCategory category,
        ProductState state,
        LocalDateTime deadline,
        List<String> imageUrls,
        int bidAmount,
        String sellerName
) {
}