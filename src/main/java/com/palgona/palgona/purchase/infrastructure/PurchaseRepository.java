package com.palgona.palgona.purchase.infrastructure;

import com.palgona.palgona.purchase.domain.Purchase;
import com.palgona.palgona.purchase.domain.PurchaseState;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PurchaseRepository extends JpaRepository<Purchase, Long>, PurchaseRepositoryCustom {

    @Query("""
        SELECT pc
        FROM Purchase pc
        JOIN FETCH pc.bidding b
        WHERE pc.state = 'WAIT'
        AND pc.deadline < CURRENT_TIMESTAMP
    """)
    List<Purchase> findExpiredPurchasesWithBidding();

    @Modifying(clearAutomatically = true)
    @Query("update Purchase pc set pc.state = :state where pc.id in :purchaseCanceledIds")
    int bulkUpdateStateToCancel(List<Long> purchaseCanceledIds, PurchaseState state);

    @Query("""
        SELECT p
        FROM Purchase p
        JOIN FETCH p.bidding b
        WHERE p.id = :id 
    """)
    Optional<Purchase> findByIdWithBidding(Long id);

}
