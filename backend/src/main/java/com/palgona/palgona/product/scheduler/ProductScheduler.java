package com.palgona.palgona.product.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.palgona.palgona.product.application.ProductService;

import lombok.RequiredArgsConstructor;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;

@Component
@RequiredArgsConstructor
public class ProductScheduler {

    private final ProductService productService;

    @Scheduled(cron = "0/30 * * * * *")
    @SchedulerLock(name = "productMetaScheduler", lockAtLeastFor = "20s", lockAtMostFor = "29s")
    public void productStatistics() {
        productService.calculateScores();
    }
}
