package com.palgona.palgona.purchase.application;

import static com.palgona.palgona.common.error.code.MemberErrorCode.MEMBER_NOT_FOUND;
import static com.palgona.palgona.common.error.code.PurchaseErrorCode.INSUFFICIENT_PERMISSION;
import static com.palgona.palgona.common.error.code.PurchaseErrorCode.PURCHASE_CANCEL_NOT_ALLOWED;
import static com.palgona.palgona.common.error.code.PurchaseErrorCode.PURCHASE_EXPIRED;
import static com.palgona.palgona.common.error.code.PurchaseErrorCode.PURCHASE_NOT_FOUND;

import com.palgona.palgona.common.dto.response.SliceResponse;
import com.palgona.palgona.common.error.exception.BusinessException;
import com.palgona.palgona.member.domain.Member;
import com.palgona.palgona.member.domain.MemberRepository;
import com.palgona.palgona.mileage.domain.Mileage;
import com.palgona.palgona.mileage.domain.MileageHistory;
import com.palgona.palgona.mileage.domain.MileageRepository;
import com.palgona.palgona.mileage.domain.MileageState;
import com.palgona.palgona.purchase.domain.Purchase;
import com.palgona.palgona.purchase.domain.PurchaseState;
import com.palgona.palgona.purchase.dto.request.PurchaseCancelRequest;
import com.palgona.palgona.purchase.dto.response.PurchaseResponse;
import com.palgona.palgona.mileage.domain.MileageHistoryRepository;
import com.palgona.palgona.purchase.infrastructure.PurchaseRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PurchaseService {

    private static final double REFUND_RATE = 0.8;

    private final PurchaseRepository purchaseRepository;
    private final MileageHistoryRepository mileageHistoryRepository;
    private final MileageRepository mileageRepository;
    private final MemberRepository memberRepository;

    public SliceResponse<PurchaseResponse> readPurchases(
            Member member,
            int pageSize,
            Long cursor
    ) {
        return purchaseRepository.findAllByMember(
                member,
                pageSize,
                cursor
        );
    }

    @Transactional
    public void confirmPurchase(Member buyer, Long id) {
        Purchase purchase = findPurchase(id);

        validatePurchasePermission(purchase, buyer);
        validateDeadline(purchase);

        purchase.confirm();

        Mileage sellerMileage = findMileageWithPessimisticLock(purchase.getSeller());
        int purchaseAmount = purchase.getPurchasePrice();
        int buyerBalance = mileageRepository.findBalanceByMember(buyer);

        mileageHistoryRepository.save(MileageHistory.of(
                sellerMileage.getBalance(),
                sellerMileage.getBalance() + purchaseAmount,
                purchaseAmount,
                MileageState.SALE,
                purchase.getSeller())
        );

        sellerMileage.charge(purchaseAmount);

        mileageHistoryRepository.save(MileageHistory.of(
                buyerBalance + purchaseAmount,
                buyerBalance,
                purchaseAmount,
                MileageState.USE,
                buyer
        ));
    }

    @Transactional
    public void cancelPurchase(
            Member member,
            Long id,
            PurchaseCancelRequest request
    ) {
        Purchase purchase = findPurchaseWithBidding(id);
        Mileage buyerMileage = findMileageWithPessimisticLock(member);

        validatePurchasePermission(purchase, member);
        validatePurchaseCancel(purchase);

        purchase.cancel();
        purchase.updateReason(request.reason());
        purchase.getBidding().cancel();

        int refundAmount = calculateRefundAmount(purchase);
        buyerMileage.refund(refundAmount);
    }

    @Transactional
    public void checkPurchaseExpiration() {
        List<Purchase> expiredPurchases = purchaseRepository.findExpiredPurchasesWithBidding();
        List<Long> purchaseCanceledIds = new ArrayList<>();

        for (Purchase expiredPurchase : expiredPurchases) {
            purchaseCanceledIds.add(expiredPurchase.getId());
            Mileage buyerMileage = findMileageWithPessimisticLock(expiredPurchase.getBuyer());

            expiredPurchase.getBidding().cancel();

            int refundAmount = calculateRefundAmount(expiredPurchase);
            buyerMileage.refund(refundAmount);

            mileageRepository.saveAndFlush(buyerMileage);
            purchaseRepository.saveAndFlush(expiredPurchase);
        }

        purchaseRepository.bulkUpdateStateToCancel(purchaseCanceledIds, PurchaseState.CANCEL);
    }

    private Purchase findPurchase(Long id) {
        return purchaseRepository.findById(id)
                .orElseThrow(() -> new BusinessException(PURCHASE_NOT_FOUND));
    }

    private Purchase findPurchaseWithBidding(Long id) {
        return purchaseRepository.findByIdWithBidding(id)
                .orElseThrow(() -> new BusinessException(PURCHASE_NOT_FOUND));
    }


    private int calculateRefundAmount(Purchase purchase) {
        return (int) (purchase.getPurchasePrice() * REFUND_RATE);
    }

    private void validateDeadline(Purchase purchase) {
        if (purchase.isDeadlineReached()) {
            throw new BusinessException(PURCHASE_EXPIRED);
        }
    }

    private void validatePurchasePermission(Purchase purchase, Member member) {
        if (!purchase.isBuyer(member)) {
            throw new BusinessException(INSUFFICIENT_PERMISSION);
        }
    }

    private Mileage findMileageWithPessimisticLock(Member member) {
        return mileageRepository.findByMemberWithPessimisticLock(member)
                .orElseThrow(() -> new BusinessException(MEMBER_NOT_FOUND));
    }

    private void validatePurchaseCancel(Purchase purchase) {
        validateDeadline(purchase);
        validatePurchaseStatusForCancel(purchase);
    }

    private void validatePurchaseStatusForCancel(Purchase purchase) {
        if (!purchase.isWaitState()) {
            throw new BusinessException(PURCHASE_CANCEL_NOT_ALLOWED);
        }
    }
}
