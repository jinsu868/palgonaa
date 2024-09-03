package com.palgona.palgona.bidding.dto.request;

public record BiddingAttemptRequest(
        Long productId,
        int price
) {
}
