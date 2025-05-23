package com.palgona.palgona.product.dto.request;

import com.palgona.palgona.product.domain.ProductCategory;

public record ProductUpdateRequest(
    String name,
    String content,
    ProductCategory category
) {
}
