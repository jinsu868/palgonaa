package com.palgona.palgona.bidding.service;

import com.palgona.palgona.product.dto.ProductWithBidInfo;
import com.palgona.palgona.purchase.domain.Purchase;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.palgona.palgona.purchase.domain.repository.PurchaseRepository;
import com.palgona.palgona.bidding.domain.Bid;
import com.palgona.palgona.bidding.domain.BidEvent;
import com.palgona.palgona.bidding.domain.BidState;
import com.palgona.palgona.bidding.domain.repository.BidEventRepository;
import com.palgona.palgona.bidding.domain.repository.BidRepository;
import com.palgona.palgona.bidding.dto.request.BidAttemptRequest;
import com.palgona.palgona.common.error.BadRequestException;
import com.palgona.palgona.common.error.ErrorCode;
import com.palgona.palgona.mileage.domain.Mileage;
import com.palgona.palgona.mileage.domain.repository.MileageRepository;
import com.palgona.palgona.product.domain.Product;
import com.palgona.palgona.product.domain.repository.ProductRepository;
import com.palgona.palgona.user.domain.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BidService {

    private final ProductRepository productRepository;
    private final BidRepository bidRepository;
    private final MileageRepository mileageRepository;
    private final BidEventRepository bidEventRepository;
    private final PurchaseRepository purchaseRepository;

    /**
     * event sourcing (Command)
     * 현재 BidSnapshot 의 입찰가보다 높은 요청의 경우 BidEvent 에 Append-only 로 쌓기 (Lock 필요X)
     * 모든 입찰에 대한 Event 를 기록해두고 일정 주기 Batch 를 통해서 현재 입찰 상태를 갱신
     * -> Write 할 때 동시성을 고려 필요 X
     */
    @Transactional
    public void attempt(
            User user,
            Long productId,
            BidAttemptRequest request
    ) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BadRequestException(ErrorCode.PRODUCT_NOT_FOUND));

        if (product.isDeadlineReached()) {
            throw new BadRequestException(ErrorCode.EXPIRE_PRODUCT);
        }

        Optional<Bid> bid = bidRepository.findByProductId(productId);

        Mileage mileage = mileageRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BadRequestException(ErrorCode.NOT_FOUND_MILEAGE));

        if (!mileage.isPayable(request.amount())) {
            throw new BadRequestException(ErrorCode.NOT_ENOUGH_BALANCE);
        }

        // 상품에 대한 최초 입찰이 시도되는 경우
        if (bid.isEmpty()) {
            bidRepository.save(
                    Bid.builder()
                    .userId(user.getId())
                    .amount(request.amount())
                    .state(BidState.ATTEMPT)
                    .productId(productId)
                    .build()
            );
            bidEventRepository.save(BidEvent.builder()
                    .amount(request.amount())
                    .userId(user.getId())
                    .productId(productId)
                    .build()
            );

            return;
        }

        Bid currentBid = bid.get();
        if (currentBid.getAmount() >= request.amount()) {
            throw new BadRequestException(ErrorCode.INVALID_ATTEMPT_AMOUNT);
        }

        bidEventRepository.save(BidEvent.builder()
                .amount(request.amount())
                .userId(user.getId())
                .productId(productId)
                .build()
        );
    }

    @Transactional
    public void snapshotBids() {
        var products = productRepository.findAllOngoing();
        var productIds = extractProductIds(products);
        var bidEvents = bidEventRepository.findMaxBidEventProductIdIn(productIds);
        bidRepository.bulkUpdateBids(bidEvents);
    }

    @Transactional
    public void checkExpireAndInsertPurchase() {
        var products = productRepository.findProductsToExpireWithBidAmount();
        var productIds = extractProductIdsFromProductBidInfos(products);

        productRepository.updateToExpireInProductIds(productIds);
        List<Purchase> purchases = products.stream()
                .map(productInfo -> Purchase.builder()
                        .productId(productInfo.productId())
                        .amount(productInfo.amount())
                        .userId(productInfo.userId())
                        .build())
                .toList();

        purchaseRepository.bulkInsert(purchases);
    }

    private List<Long> extractProductIds(List<Product> products) {
        return products.stream()
                .map(Product::getId)
                .toList();
    }

    private List<Long> extractProductIdsFromProductBidInfos(List<ProductWithBidInfo> products) {
        return products.stream()
                .map(ProductWithBidInfo::productId)
                .toList();
    }
}
