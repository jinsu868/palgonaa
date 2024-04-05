package com.palgona.palgona.repository;

import com.palgona.palgona.domain.bidding.Bidding;
import com.palgona.palgona.domain.member.Member;
import com.palgona.palgona.domain.product.Product;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BiddingRepository extends JpaRepository<Bidding, Long> {

    Page<Bidding> findAllByProduct(Pageable pageable, Product product);

    @Lock(LockModeType.OPTIMISTIC)
    @Query("SELECT b FROM Bidding b "
            + "JOIN FETCH b.member m "
            + "WHERE b.state = 'ATTEMPT' AND b.product.deadline <= :currentDateTime")
    List<Bidding> findExpiredBiddingsWithOptimisticLock(@Param("currentDateTime") LocalDateTime currentDateTime);
    boolean existsByProduct(Product product);

    Optional<Integer> findHighestPriceByProduct(Product product);

    boolean existsByMember(Member member);

    Optional<Integer> findHighestPriceByMember(Member member);
}
