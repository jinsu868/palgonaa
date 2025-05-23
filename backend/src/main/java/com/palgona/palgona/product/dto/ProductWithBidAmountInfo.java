package com.palgona.palgona.product.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ProductWithBidAmountInfo(
        Long id,
        String name,
        int currentBid,
        LocalDateTime deadline,
        List<String> imageUrl,
        String sellerNickname
) {
}
