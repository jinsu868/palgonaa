package com.palgona.palgona.payment.domain.repository;

import org.springframework.stereotype.Repository;

import com.palgona.palgona.payment.domain.PaymentReceipt;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class PaymentReceiptRepository {

    private final PaymentReceiptJpaRepository paymentReceiptJpaRepository;

    public boolean existsByImpUid(String impUid) {
        return paymentReceiptJpaRepository.existsByImpUid(impUid);
    }

    public PaymentReceipt save(PaymentReceipt paymentReceipt) {
        return paymentReceiptJpaRepository.save(paymentReceipt);
    }
}
