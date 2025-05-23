package com.palgona.palgona.bidding.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.palgona.palgona.bidding.service.BidService;

import lombok.RequiredArgsConstructor;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;

@Component
@RequiredArgsConstructor
public class BidScheduler {

    private final BidService bidService;

    /**
     * Scheduler Common Consideration :
     * 스케줄러가 3분안에 끝나지 않으면? (Throughput 을 늘리기 위해 생각해야 함.)
     * 1. 범위를 나눠서 DB 커넥션 N개로 병렬로 Update 치면 Throughput 을 높일 수 있음.
     * 2. 물리적인 장비 (DB의 CPU, Memory)의 부족으로 처리량이 제한되면 Sharding 을 고려할 수 있음.
     */

    /**
     * event sourcing (Read snapshot 갱신)
     */
    @Scheduled(cron = "0 * */3 * * *")
    @SchedulerLock(name = "bidSnapshotScheduler", lockAtLeastFor = "PT1M", lockAtMostFor = "PT2M")
    public void snapshotBids() {
        bidService.snapshotBids();
    }

    /**
     * 상품 입찰 기간 만료 처리를 해주는 Scheduler
     * 기간 만료된 상품들에 대해 구매 내역을 생성해줌.
     * Purchase 를 통해 구매 확정 및 결제가 진행된다.
     */
    @Scheduled(cron = "0 */5 * * * *")
    @SchedulerLock(name = "bidExpireScheduler", lockAtLeastFor = "PT3M", lockAtMostFor = "PT4M")
    public void checkExpiredBids() {
        bidService.checkExpireAndInsertPurchase();
    }
}
