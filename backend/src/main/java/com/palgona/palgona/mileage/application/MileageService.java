package com.palgona.palgona.mileage.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.palgona.palgona.payment.domain.PaymentReceipt;
import com.palgona.palgona.payment.domain.repository.PaymentReceiptRepository;
import com.siot.IamportRestClient.response.Payment;
import com.palgona.palgona.common.error.BadRequestException;
import com.palgona.palgona.common.error.ErrorCode;
import com.palgona.palgona.mileage.domain.Mileage;
import com.palgona.palgona.mileage.domain.repository.MileageRepository;
import com.palgona.palgona.user.domain.User;
import com.palgona.palgona.transaction.domain.Transaction;
import com.palgona.palgona.transaction.domain.TransactionType;
import com.palgona.palgona.transaction.domain.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MileageService {

    private final MileageRepository mileageRepository;
    private final TransactionRepository transactionRepository;
    private final PaymentReceiptRepository paymentReceiptRepository;

    @Transactional
    public void charge(int amount, User user, Payment receivedPayment) {

        /**
         * purchaseService::updateState 와의 경합 고려.
         * Mileage 충전자가 판매자일 때 상품 구매 확정 과정에서 판매자의 Mileage 갱신과 충돌 가능성 존재.
         * 동시 요청 수는 최대 2개임. (TX 를 물고 세션에서 락 획득 대기 시간이 짧을 것으로 판단.
         * SELECT FOR UPDATE 를 사용해도 DB Connection Starvation 문제는 거의 발생하지 않을 것으로 판단.
         */
        Mileage mileage = mileageRepository.findByUserIdWithLock(user.getId())
                        .orElseThrow(() -> new BadRequestException(ErrorCode.NOT_FOUND_MILEAGE));

        mileage.charge(amount);

        transactionRepository.save(
                Transaction.builder()
                        .userId(user.getId())
                        .type(TransactionType.WITHDRAWAL)
                        .amount(amount)
                        .build()
        );

        paymentReceiptRepository.save(
                PaymentReceipt.builder()
                        .impUid(receivedPayment.getImpUid())
                        .merchantUid(receivedPayment.getMerchantUid())
                        .receiptUrl(receivedPayment.getReceiptUrl())
                        .amount(receivedPayment.getAmount())
                        .build()
        );
    }
}
