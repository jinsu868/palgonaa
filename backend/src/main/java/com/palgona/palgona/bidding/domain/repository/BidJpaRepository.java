package com.palgona.palgona.bidding.domain.repository;

import com.palgona.palgona.bidding.domain.Bid;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BidJpaRepository extends JpaRepository<Bid, Long> {
    Optional<Bid> findByProductId(Long productId);
}
