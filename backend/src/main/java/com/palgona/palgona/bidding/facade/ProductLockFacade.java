package com.palgona.palgona.bidding.facade;

import com.palgona.palgona.bidding.application.BiddingService;
import com.palgona.palgona.product.domain.ProductRepository;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.RedissonMultiLock;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductLockFacade {

    private static final String BIDDING_LOCK_PREFIX = "LOCK:";

    private final BiddingService biddingService;
    private final ProductRepository productRepository;
    private final RedissonClient redissonClient;

    public void checkBiddingExpiration() {
        List<RLock> keys = productRepository.findAuctionEndedProductIdsInOnSaleState()
                .stream()
                .map(id -> redissonClient.getLock(BIDDING_LOCK_PREFIX + id))
                .toList();

        if (keys.isEmpty()) {
            return;
        }

        RedissonMultiLock rMultiLock = new RedissonMultiLock(keys.toArray(new RLock[0]));

        try {
            boolean available = rMultiLock.tryLock(5, 3, TimeUnit.SECONDS);

            if (!available) {
                throw new IllegalArgumentException("failed acquire rMultiLock");
            }

            biddingService.checkBiddingExpiration();
        } catch (InterruptedException e) {
            throw new IllegalArgumentException(e);
        } finally {
            try {
                rMultiLock.unlock();
            } catch (IllegalMonitorStateException e) {
                log.info("already unLock");
            }
        }
    }
}
