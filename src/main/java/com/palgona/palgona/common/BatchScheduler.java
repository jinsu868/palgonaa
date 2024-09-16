package com.palgona.palgona.common;

import com.palgona.palgona.bidding.facade.ProductLockFacade;
import com.palgona.palgona.purchase.application.PurchaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BatchScheduler {

    private final ProductLockFacade productLockFacade;
    private final PurchaseService purchaseService;

    @Scheduled(cron = "0 * * * * *")
    public void checkBiddingExpiration() {
        productLockFacade.checkBiddingExpiration();
    }

    @Scheduled(cron = "0 * * * * *")
    public void checkPurchaseExpiration() {
        purchaseService.checkPurchaseExpiration();
    }
}
