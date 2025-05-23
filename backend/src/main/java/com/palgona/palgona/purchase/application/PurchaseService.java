package com.palgona.palgona.purchase.application;

import com.palgona.palgona.product.domain.Product;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.palgona.palgona.product.domain.repository.ProductRepository;
import com.palgona.palgona.mileage.domain.Mileage;
import com.palgona.palgona.common.error.BadRequestException;
import com.palgona.palgona.common.error.ErrorCode;
import com.palgona.palgona.mileage.domain.repository.MileageRepository;
import com.palgona.palgona.purchase.domain.Purchase;
import com.palgona.palgona.common.model.PageInfo;
import com.palgona.palgona.purchase.domain.PurchaseState;
import com.palgona.palgona.purchase.dto.response.PurchaseResponse;
import com.palgona.palgona.user.domain.User;
import com.palgona.palgona.purchase.domain.repository.PurchaseRepository;
import com.palgona.palgona.purchase.dto.request.PurchaseUpdateRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final MileageRepository mileageRepository;
    private final ProductRepository productRepository;

    public PageInfo<PurchaseResponse> findAll(User user, String pageToken, PurchaseState state) {
        return purchaseRepository.findAllByUserIdWithPage(user.getId(), pageToken, state);
    }

    @Transactional
    public void updateState(User user, PurchaseUpdateRequest request, Long purchaseId) {
        Purchase purchase = purchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new BadRequestException(ErrorCode.PURCHASE_NOT_FOUND));

        if (!purchase.isOwner(user.getId())) {
            throw new BadRequestException(ErrorCode.PURCHASE_NOT_OWNED);
        }

        if (!purchase.isOngoing()) {
            throw new BadRequestException(ErrorCode.PURCHASE_NOT_ONGOING);
        }

        if (request.state() == PurchaseState.ONGOING || request.state() == PurchaseState.EXPIRED) {
            throw new BadRequestException(ErrorCode.INVALID_PURCHASE_UPDATE);
        }

        Mileage buyerMileage = mileageRepository.findByUserIdWithLock(user.getId())
                .orElseThrow(() -> new BadRequestException(ErrorCode.NOT_FOUND_MILEAGE));

        Product product = productRepository.findById(purchase.getProductId())
                .orElseThrow(() -> new BadRequestException(ErrorCode.PRODUCT_NOT_FOUND));

        Mileage sellerMileage = mileageRepository.findByUserIdWithLock(product.getUserId())
                        .orElseThrow(() -> new BadRequestException(ErrorCode.NOT_FOUND_MILEAGE));

        mileageRepository.findByUserIdWithLock(user.getId())
                .orElseThrow(() -> new BadRequestException(ErrorCode.NOT_FOUND_MILEAGE));

        if (request.state() == PurchaseState.COMPLETED) {
            if (!buyerMileage.isPayable(purchase.getAmount())) {
                throw new BadRequestException(ErrorCode.NOT_ENOUGH_BALANCE);
            }

            buyerMileage.use(purchase.getAmount());
            sellerMileage.charge(purchase.getAmount());
            purchase.complete();
        }

        /**
         * UX 측면에서 어뷰징 방지를 위한 경고 시스템 도입하면 좋을듯?
         * 한달에 2번 이상 CANCEL 할 경우 -> 패널티
         */
        if (request.state() == PurchaseState.CANCEL) {
            purchase.cancel();
        }
    }

    public void checkExpirePurchase() {
        purchaseRepository.updateAllExpiredPurchase(LocalDateTime.now().minusDays(1));
    }
}
