package com.palgona.palgona.bidding.presentation;

import com.palgona.palgona.common.dto.CustomMemberDetails;
import com.palgona.palgona.bidding.domain.Bidding;
import com.palgona.palgona.bidding.dto.request.BiddingAttemptRequest;
import com.palgona.palgona.bidding.dto.response.BiddingPageResponse;
import com.palgona.palgona.bidding.application.BiddingService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/biddings")
public class BiddingController {
    private final BiddingService biddingService;

    @PostMapping(value = "/attempt", consumes = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(summary = "입찰 진행 api", description = "물건 id와 가격을 받아서 입찰을 진행한다.")
    public ResponseEntity<Void> attemptBidding(
            @AuthenticationPrincipal CustomMemberDetails loginMember,
            @RequestBody BiddingAttemptRequest request
    ) {
        biddingService.attemptBidding(
                request.productId(),
                loginMember.getMember(),
                request.price()
        );
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{productId}")
    @Operation(summary = "입찰 목록 api", description = "물건 id를 받아서 입찰 목록을 보여준다.")
    public ResponseEntity<BiddingPageResponse> findAllByProductId(
            @PathVariable long productId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<Bidding> biddings =  biddingService.findAllByProductId(productId, pageable);
        BiddingPageResponse response = BiddingPageResponse.of(biddings);

        return ResponseEntity.ok(response);
    }
}
