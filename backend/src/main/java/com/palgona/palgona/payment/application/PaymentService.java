package com.palgona.palgona.payment.application;

import java.io.IOException;

import org.springframework.stereotype.Component;

import com.palgona.palgona.common.error.BadRequestException;
import com.palgona.palgona.common.error.ErrorCode;
import com.palgona.palgona.payment.domain.PaymentReceipt;
import com.palgona.palgona.payment.domain.repository.PaymentReceiptRepository;
import com.palgona.palgona.payment.dto.request.PaymentRequest;
import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.response.Payment;

import lombok.RequiredArgsConstructor;


@Component
@RequiredArgsConstructor
public class PaymentService {

    private final IamportClient iamportClient;
    private final PaymentReceiptRepository paymentReceiptRepository;

    public Payment validate(PaymentRequest paymentRequest) {
        Payment receivedcPayment;
        try {
            receivedcPayment = iamportClient.paymentByImpUid(paymentRequest.impUid()).getResponse();
        } catch (IamportResponseException | IOException e) {
            throw new BadRequestException(ErrorCode.FAILED_TO_VALIDATE_PAYMENT);
        }

        if (!receivedcPayment.getImpUid().equals(paymentRequest.impUid())) {
            throw new BadRequestException(ErrorCode.FAILED_TO_VALIDATE_PAYMENT);
        }

        if (!paymentReceiptRepository.existsByImpUid(paymentRequest.impUid())) {
            throw new BadRequestException(ErrorCode.ALREADY_CHARGED_REQUEST);
        }

        return receivedcPayment;
    }
}
