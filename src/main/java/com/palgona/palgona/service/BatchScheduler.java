package com.palgona.palgona.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BatchScheduler {

    private final BiddingService biddingService;
    private final PurchaseService purchaseService;

    @Scheduled(cron = "0 * * * * *")
    public void checkBiddingExpiration() {
        biddingService.checkBiddingExpiration();
    }

    @Scheduled(cron = "0 * * * * *")
    public void checkPurchaseExpiration() {
        purchaseService.checkPurchaseExpiration();
    }
}
