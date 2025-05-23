package com.palgona.palgona.bidding.domain.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.palgona.palgona.bidding.domain.BidEvent;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class BidEventRepository {

    private final BidJdbcRepository bidJdbcRepository;

    public void save(BidEvent bidEvent) {
        bidJdbcRepository.insert(bidEvent);
    }

    public List<BidEvent> findMaxBidEventProductIdIn(List<Long> productIds) {
        return bidJdbcRepository.selectMaxBidEventGroupByProductId(productIds);
    }
}
