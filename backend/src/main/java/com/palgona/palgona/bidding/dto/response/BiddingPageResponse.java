package com.palgona.palgona.bidding.dto.response;

import com.palgona.palgona.bidding.domain.Bidding;
import java.util.List;
import org.springframework.data.domain.Page;

public record BiddingPageResponse(long total, int pages, List<Bidding> biddingList) {
    public static BiddingPageResponse of(Page<Bidding> page) {
        return new BiddingPageResponse(page.getTotalElements(), page.getTotalPages(), page.toList());
    }
}
