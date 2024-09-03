package com.palgona.palgona.product.dto.response;

import com.palgona.palgona.product.infrastructure.querydto.ProductDetailQueryResponse;

import java.time.LocalDateTime;
import java.util.List;

public record ProductDetailResponse(
        Long productId,
        String productName,
        String content,
        String category,
        String productState,
        LocalDateTime deadline,
        LocalDateTime created_at,
        Long ownerId,
        String ownerName,
        String ownerImgUrl,
        Integer highestPrice,
        Long bookmarkCount,
        Long chatroomCount,
        List<String> imageUrls,
        boolean isSilent
) {
    public static ProductDetailResponse from(
            ProductDetailQueryResponse queryResponse,
            List<String> files
            ){
        return new ProductDetailResponse(
                queryResponse.productId(),
                queryResponse.productName(),
                queryResponse.content(),
                queryResponse.category().getKey(),
                queryResponse.productState().getKey(),
                queryResponse.deadline(),
                queryResponse.created_at(),
                queryResponse.ownerId(),
                queryResponse.ownerName(),
                queryResponse.ownerImgUrl(),
                queryResponse.highestBid(),
                queryResponse.bookmarkCount(),
                queryResponse.chatroomCount(),
                files,
                queryResponse.isSilent()
        );
    }

}