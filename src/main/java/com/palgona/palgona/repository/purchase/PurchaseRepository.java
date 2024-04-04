package com.palgona.palgona.repository.purchase;

import com.palgona.palgona.domain.purchase.Purchase;
import com.palgona.palgona.domain.purchase.PurchaseState;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PurchaseRepository extends JpaRepository<Purchase, Long>, PurchaseRepositoryCustom {

    @Query("select pc from Purchase pc join fetch pc.bidding b join fetch b.member m")
    Optional<Purchase> findByIdWithSeller(Long id);

    @Query("select pc from Purchase pc join fetch pc.member m")
    Optional<Purchase> findByIdWithBuyer(Long id);

    @Query("select pc from Purchase pc "
            + "join fetch pc.member m "
            + "where pc.state = 'WAIT' and pc.deadline < :currentDateTime")
    List<Purchase> findAllByDeadline(@Param("currentDateTime") LocalDateTime currentDateTime);

    @Modifying(clearAutomatically = true)
    @Query("update Purchase pc set pc.state = :state where pc.id in :purchaseCanceledIds")
    int bulkUpdateState(List<Long> purchaseCanceledIds, PurchaseState state);
}
