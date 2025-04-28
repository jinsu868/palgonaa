package com.palgona.palgona.purchase.presentation;

import com.palgona.palgona.common.dto.CustomMemberDetails;
import com.palgona.palgona.common.dto.response.SliceResponse;
import com.palgona.palgona.purchase.dto.request.PurchaseCancelRequest;
import com.palgona.palgona.purchase.dto.response.PurchaseResponse;
import com.palgona.palgona.purchase.application.PurchaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/purchases")
public class PurchaseController {

    private final PurchaseService purchaseService;

    @GetMapping
    public ResponseEntity<SliceResponse<PurchaseResponse>> readPurchases(
            @AuthenticationPrincipal CustomMemberDetails member,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) Long cursor
    ) {

        SliceResponse<PurchaseResponse> response = purchaseService.readPurchases(
                member.getMember(),
                pageSize,
                cursor
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{purchaseId}/confirm")
    public ResponseEntity<Void> confirmPurchase(
            @AuthenticationPrincipal CustomMemberDetails member,
            @PathVariable Long purchaseId
    ) {

        purchaseService.confirmPurchase(
                member.getMember(),
                purchaseId
        );

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{purchaseId}/cancel")
    public ResponseEntity<Void> cancelPurchase(
            @AuthenticationPrincipal CustomMemberDetails member,
            @PathVariable Long purchaseId,
            @RequestBody PurchaseCancelRequest request
    ) {

        purchaseService.cancelPurchase(
                member.getMember(),
                purchaseId,
                request
        );

        return ResponseEntity.ok().build();
    }
}
