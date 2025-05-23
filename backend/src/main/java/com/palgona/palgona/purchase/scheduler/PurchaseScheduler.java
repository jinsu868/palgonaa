package com.palgona.palgona.purchase.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.palgona.palgona.purchase.application.PurchaseService;

import lombok.RequiredArgsConstructor;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;

@Component
@RequiredArgsConstructor
public class PurchaseScheduler {

    private final PurchaseService purchaseService;

    @Scheduled(cron = "0 */3 * * * *")
    @SchedulerLock(name = "purchaseExpireScheduler", lockAtLeastFor = "PT1M", lockAtMostFor = "PT2M")
    public void expirePurchase() {
        purchaseService.checkExpirePurchase();
    }
}
