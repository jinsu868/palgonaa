package com.palgona.palgona.purchase.domain.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.palgona.palgona.purchase.domain.Purchase;
import com.palgona.palgona.common.model.PageInfo;
import com.palgona.palgona.purchase.domain.PurchaseState;
import com.palgona.palgona.purchase.dto.response.PurchaseResponse;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class PurchaseRepository {

    private static final int DEFAULT_PAGE_SIZE = 10;

    private final PurchaseJpaRepository purchaseJpaRepository;
    private final PurchaseJdbcRepository purchaseJdbcRepository;

    public PageInfo<PurchaseResponse> findAllByUserIdWithPage(
            Long userId,
            String pageToken,
            PurchaseState state
    ) {
        var data = purchaseJpaRepository.findAllByUserId(userId, pageToken, state, DEFAULT_PAGE_SIZE);
        return PageInfo.of(data, DEFAULT_PAGE_SIZE, PurchaseResponse::id);
    }

    public Optional<Purchase> findById(Long purchaseId) {
        return purchaseJpaRepository.findById(purchaseId);
    }

    public void updateAllExpiredPurchase(LocalDateTime expiredLocalDateTime) {
        purchaseJpaRepository.updateExpiredPurchase(expiredLocalDateTime);
    }

    public void bulkInsert(List<Purchase> purchases) {
        purchaseJdbcRepository.bulkInsert(purchases);
    }
}
