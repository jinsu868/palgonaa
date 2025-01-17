package com.palgona.palgona.bidding.domain;

import com.palgona.palgona.member.domain.Member;
import com.palgona.palgona.product.domain.Product;
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

    @Query("""
        SELECT MAX(b.price)
        FROM Bidding b
        WHERE b.product = :product
        """)
    Optional<Integer> findHighestPriceByProduct(Product product);

    boolean existsByProduct(Product product);

    @Query("""
       SELECT MAX(b.price)
       FROM Bidding b
       WHERE b.member = :member
       """)
    Optional<Integer> findHighestPriceByMember(Member member);


    @Query(value = """
       SELECT b
       FROM Bidding b
       JOIN FETCH b.member m
       WHERE b.product = :product
       ORDER BY b.id
       DESC
       """)
    List<Bidding> findByProduct(Product product);

    @Query("""
      SELECT b
      FROM Bidding b
      WHERE b.price = (
            SELECT MAX(bb.price)
            FROM Bidding bb
            WHERE bb.product = :product
            )
     AND b.product = :product
    """)
    Optional<Bidding> findHighestPriceBiddingByProduct(Product product);

    @Query(value = """
        SELECT b
        FROM bidding b
        JOIN member m
        ON b.member_id = :memberId
        ORDER BY b.price DESC
        LIMIT 1
    """, nativeQuery = true)
    Optional<Bidding> findHighestBeforeBiddingByMember(Long memberId);
}
