package com.palgona.palgona.purchase.application;

import static com.palgona.palgona.common.error.code.PurchaseErrorCode.PURCHASE_NOT_FOUND;

import com.palgona.palgona.common.dto.response.SliceResponse;
import com.palgona.palgona.common.error.exception.BusinessException;
import com.palgona.palgona.member.domain.Member;
import com.palgona.palgona.mileage.domain.MileageHistory;
import com.palgona.palgona.mileage.domain.MileageState;
import com.palgona.palgona.purchase.domain.Purchase;
import com.palgona.palgona.purchase.domain.PurchaseState;
import com.palgona.palgona.purchase.dto.request.PurchaseCancelRequest;
import com.palgona.palgona.purchase.dto.response.PurchaseResponse;
import com.palgona.palgona.mileage.domain.MileageHistoryRepository;
import com.palgona.palgona.purchase.infrastructure.PurchaseRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PurchaseService {

    private static final double REFUND_RATE = 0.8;

    private final PurchaseRepository purchaseRepository;

    private final MileageHistoryRepository mileageHistoryRepository;

    public SliceResponse<PurchaseResponse> readPurchases(Member member, int pageSize, String cursor) {
        return purchaseRepository.findAllByMember(member, pageSize, cursor);
    }

    @Transactional
    public void confirmPurchase(Member member, Long id) {
        Purchase purchase = findPurchaseWithSellerAndPessimisticLock(id);
        purchase.validateOwner(member);
        purchase.validateDeadline(LocalDateTime.now());
        purchase.confirm();
        Member seller = purchase.getBidding().getMember();
        int purchaseAmount = purchase.getPurchasePrice();
        seller.receivePayment(purchaseAmount);

        mileageHistoryRepository.save(MileageHistory.builder()
                .beforeMileage(seller.getMileage() - purchaseAmount)
                .afterMileage(seller.getMileage())
                .amount(purchaseAmount)
                .state(MileageState.SALE)
                .member(seller)
                .build());

        mileageHistoryRepository.save(MileageHistory.builder()
                .beforeMileage(member.getMileage() + purchaseAmount)
                .afterMileage(member.getMileage())
                .amount(purchaseAmount)
                .state(MileageState.USE)
                .member(member)
                .build());
    }

    @Transactional
    public void cancelPurchase(Member member, Long id, PurchaseCancelRequest request) {
        Purchase purchase = findPurchaseWithBuyerAndPessimisticLock(id);
        purchase.validateOwner(member);
        purchase.validateDeadline(LocalDateTime.now());
        purchase.cancel();
        purchase.updateReason(request.reason());
        Member buyer = purchase.getMember();
        int refundAmount = calculateRefundAmount(purchase);
        buyer.refundMileage(refundAmount);
    }

    @Transactional
    public void checkPurchaseExpiration() {
        List<Purchase> expiredPurchases = purchaseRepository.findAllByDeadline(LocalDateTime.now());
        List<Long> purchaseCanceledIds = new ArrayList<>();
        for (Purchase expiredPurchase : expiredPurchases) {
            purchaseCanceledIds.add(expiredPurchase.getId());
            Member buyer = expiredPurchase.getMember();
            int refundAmount = calculateRefundAmount(expiredPurchase);
            buyer.refundMileage(refundAmount);
        }

        purchaseRepository.bulkUpdateState(purchaseCanceledIds, PurchaseState.CANCEL);
    }

    private Purchase findPurchaseWithBuyerAndPessimisticLock(Long id) {
        return purchaseRepository.findByIdWithBuyerAndPessimisticLock(id)
                .orElseThrow(() -> new BusinessException(PURCHASE_NOT_FOUND));
    }

    private Purchase findPurchaseWithSellerAndPessimisticLock(Long id) {
        return purchaseRepository.findByIdWithSellerAndPessimisticLock(id)
                .orElseThrow(() -> new BusinessException(PURCHASE_NOT_FOUND));
    }

    private int calculateRefundAmount(Purchase purchase) {
        return (int) (purchase.getPurchasePrice() * REFUND_RATE);
    }
}
