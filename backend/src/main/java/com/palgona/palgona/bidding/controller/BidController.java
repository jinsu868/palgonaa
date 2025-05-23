package com.palgona.palgona.bidding.controller;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.palgona.palgona.auth.annotation.AuthUser;
import com.palgona.palgona.bidding.dto.request.BidAttemptRequest;
import com.palgona.palgona.bidding.service.BidService;
import com.palgona.palgona.user.domain.User;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/bids")
public class BidController {

    private final BidService bidService;

    /**
     * Redis 싱글 스레드 원자성을 활용한 Distributed Lock 동시 입찰 방지 방식에서 Event Sourcing Pattern 으로 Refactor
     * 모든 bid_event 를 Append only 로 DB 에 쌓아두고 짧은 주기로 Batch Job 을 통해 bid (snapshot) 에 UPDATE
     */
    @PostMapping("/{productId}/attempt")
    public ResponseEntity<Void> attempt(
            @AuthUser User user,
            @PathVariable Long productId,
            @RequestBody BidAttemptRequest request
    ) {
        bidService.attempt(user, productId, request);
        return ResponseEntity.created(URI.create("/api/v1/bids/" + productId + "/attempt")).build();
    }
}
