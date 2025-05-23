package com.palgona.palgona.bidding.domain.repository;

import com.palgona.palgona.bidding.domain.Bid;
import com.palgona.palgona.bidding.domain.BidEvent;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class BidRepository {

    private final BidJpaRepository bidJpaRepository;
    private final BidJdbcRepository bidJdbcRepository;

    public Optional<Bid> findByProductId(Long productId) {
        return bidJpaRepository.findByProductId(productId);
    }

    public Bid save(Bid bid) {
        return bidJpaRepository.save(bid);
    }

    public void bulkUpdateBids(List<BidEvent> bidEvents) {
        bidJdbcRepository.bulkUpdateBids(bidEvents);
    }
}
