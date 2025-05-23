package com.palgona.palgona.payment.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.palgona.palgona.payment.domain.PaymentReceipt;

public interface PaymentReceiptJpaRepository extends JpaRepository<PaymentReceipt, Long> {
    boolean existsByImpUid(String impUid);
}
