package com.palgona.palgona.payment.dto.request;

public record PaymentRequest(
        String impUid,
        Integer amount
) {
}
