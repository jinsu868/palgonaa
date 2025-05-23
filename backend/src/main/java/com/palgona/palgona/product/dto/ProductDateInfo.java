package com.palgona.palgona.product.dto;

import java.time.LocalDateTime;

public record ProductDateInfo(
        Long id,
        LocalDateTime createdAt
) {
}
