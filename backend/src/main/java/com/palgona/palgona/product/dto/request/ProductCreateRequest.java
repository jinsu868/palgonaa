package com.palgona.palgona.product.dto.request;


import com.palgona.palgona.product.domain.ProductCategory;
import java.time.LocalDateTime;

public record ProductCreateRequest(
        String name,
        Integer initialPrice,
        String content,
        ProductCategory category,
        LocalDateTime deadline
) {
}
