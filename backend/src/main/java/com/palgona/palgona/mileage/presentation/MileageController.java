package com.palgona.palgona.mileage.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.siot.IamportRestClient.response.Payment;
import com.palgona.palgona.mileage.application.MileageService;
import com.palgona.palgona.auth.annotation.AuthUser;
import com.palgona.palgona.payment.application.PaymentService;
import com.palgona.palgona.payment.dto.request.PaymentRequest;
import com.palgona.palgona.user.domain.User;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/mileages")
public class MileageController {
    private final MileageService mileageService;
    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<Void> chargeMileage(
            @RequestBody PaymentRequest request,
            @AuthUser User user
    ){
        /**
         * 네트워크 타고 검증하는 것을 다 끝내고 TX 를 열어서 영수증 저장과 Mileage UPDATE 를 묶음.
         */
        Payment receivedPayment = paymentService.validate(request);
        mileageService.charge(request.amount(), user, receivedPayment);

        return ResponseEntity.ok().build();
    }
}
