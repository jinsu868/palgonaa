package com.palgona.palgona.product.dto;

import com.palgona.palgona.product.domain.ProductCategory;
import com.palgona.palgona.product.domain.ProductState;
import java.time.LocalDateTime;
import java.util.List;

public record ProductDetailInfo(
        Long id,
        ProductCategory category,
        String name,
        String content,
        LocalDateTime deadline,
        ProductState state,
        List<String> imageUrls,
        Long userId,
        String nickname,
        String profileImage,
        int bookmarkCount
) {
}
