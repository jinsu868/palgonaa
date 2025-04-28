package com.palgona.palgona.product.dto.request;


import java.time.LocalDateTime;

public record ProductCreateRequestWithoutImage(
        String name,
        Integer initialPrice,
        String content,
        String category,
        LocalDateTime deadline
) {
}
