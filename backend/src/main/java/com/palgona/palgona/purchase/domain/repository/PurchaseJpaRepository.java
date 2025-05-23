package com.palgona.palgona.purchase.domain.repository;

import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;

import com.palgona.palgona.purchase.domain.Purchase;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface PurchaseJpaRepository extends JpaRepository<Purchase, Long>, PurchaseQueryRepository {

    @Modifying
    @Query("UPDATE Purchase p SET p.state = 'EXPIRED' WHERE p.state = 'ONGOING' AND p.createdAt <= :expiredLocalDateTime")
    int updateExpiredPurchase(LocalDateTime expiredLocalDateTime);
}
