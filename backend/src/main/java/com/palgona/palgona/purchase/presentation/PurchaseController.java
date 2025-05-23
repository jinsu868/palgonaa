package com.palgona.palgona.purchase.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.palgona.palgona.purchase.dto.request.PurchaseUpdateRequest;
import com.palgona.palgona.auth.annotation.AuthUser;
import com.palgona.palgona.common.model.PageInfo;
import com.palgona.palgona.purchase.domain.PurchaseState;
import com.palgona.palgona.purchase.dto.response.PurchaseResponse;
import com.palgona.palgona.user.domain.User;
import com.palgona.palgona.purchase.application.PurchaseService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/purchases")
public class PurchaseController {

    private final PurchaseService purchaseService;

    /**
     * DEFAULT RESULT : ONGOING 구매 내역 조회
     * Possible PurchaseState : COMPLETED, ONGOING, CANCEL
     */
    @GetMapping
    public ResponseEntity<PageInfo<PurchaseResponse>> findAll(
            @AuthUser User user,
            @RequestParam(required = false) String pageToken,
            @RequestParam(required = false) PurchaseState state
    ) {
        return ResponseEntity.ok(purchaseService.findAll(user, pageToken, state));
    }

    /**
     * CONFIRM OR REJECT
     * REJECT 의 경우 판매자의 UX 측면에서 경고 시스템을 도입해서 패널티를 주는 방식을 고려
     */
   @PostMapping("/{purchaseId}")
    public ResponseEntity<Void> update(
           @AuthUser User user,
           @RequestBody PurchaseUpdateRequest request,
           @PathVariable Long purchaseId
   ) {
       purchaseService.updateState(user, request, purchaseId);
       return ResponseEntity.noContent().build();
   }
}
