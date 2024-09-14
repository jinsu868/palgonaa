package com.palgona.palgona.bidding.application;

import static com.palgona.palgona.common.error.code.BiddingErrorCode.BIDDING_EXPIRED_PRODUCT;
import static com.palgona.palgona.common.error.code.BiddingErrorCode.BIDDING_INSUFFICIENT_BID;
import static com.palgona.palgona.common.error.code.BiddingErrorCode.BIDDING_LOWER_PRICE;
import static com.palgona.palgona.common.error.code.ProductErrorCode.NOT_FOUND;

import com.palgona.palgona.common.annotation.DistributedLock;
import com.palgona.palgona.common.error.exception.BusinessException;
import com.palgona.palgona.bidding.domain.Bidding;
import com.palgona.palgona.member.domain.Member;
import com.palgona.palgona.product.domain.Product;
import com.palgona.palgona.purchase.domain.Purchase;
import com.palgona.palgona.bidding.domain.BiddingRepository;
import com.palgona.palgona.purchase.infrastructure.PurchaseRepository;
import com.palgona.palgona.member.domain.MemberRepository;
import com.palgona.palgona.product.domain.ProductRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BiddingService {
    private final BiddingRepository biddingRepository;
    private final ProductRepository productRepository;
    private final PurchaseRepository purchaseRepository;
    private final MemberRepository memberRepository;

    @DistributedLock(key = "#productId")
    public void attemptBidding(Long productId, Member biddingMember, int attemptPrice) {
        Product product = findProduct(productId);

        validateProductDeadline(product);
        validateBiddingCreate(attemptPrice, product);

        int previousBid = biddingRepository.findHighestPriceByMember(biddingMember).orElse(0);
        int extraCost = attemptPrice - previousBid;
        biddingMember.useMileage(extraCost);
        Bidding bidding = Bidding.builder()
                .member(biddingMember)
                .product(product)
                .price(attemptPrice)
                .build();

        biddingRepository.save(bidding);
        memberRepository.save(biddingMember);
    }

    public Page<Bidding> findAllByProductId(long productId, Pageable pageable) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(NOT_FOUND));

        return biddingRepository.findAllByProduct(pageable, product);
    }

    @Transactional
    public void checkBiddingExpiration() {
        List<Product> auctionFinishedProduct = productRepository.findAuctionEndedProductsInOnSaleState();
        for (Product expiredProduct : auctionFinishedProduct) {
            List<Bidding> biddings = biddingRepository.findByProduct(expiredProduct);
            if (biddings.isEmpty()) {
                expiredProduct.expire();
                continue;
            }

            expiredProduct.sell();
            Bidding latestBidding = biddings.get(0);
            latestBidding.success();
            Purchase purchase = Purchase.of(
                    latestBidding.getPrice(),
                    latestBidding,
                    latestBidding.getMember(),
                    expiredProduct.getMember()
            );

            purchaseRepository.save(purchase);

            for (int i = 1; i < biddings.size(); i++) {
                Bidding failedBidding = biddings.get(i);
                Member losingMember = failedBidding.getMember();
                losingMember.refundMileage(failedBidding.getPrice());
                failedBidding.fail();
            }
        }
    }

    private void validateBiddingCreate(int attemptPrice, Product product) {
        int highestPrice = biddingRepository.findHighestPriceByProduct(product).orElse(0);
        int threshold = (int) Math.pow(10, String.valueOf(attemptPrice).length() - 2);
        int priceDifference = attemptPrice -  highestPrice;

        if (attemptPrice < product.getInitialPrice()) {
            throw new BusinessException(BIDDING_LOWER_PRICE);
        }

        if (attemptPrice <= highestPrice) {
            throw new BusinessException(BIDDING_LOWER_PRICE);
        }

        if (priceDifference < threshold) {
            throw new BusinessException(BIDDING_INSUFFICIENT_BID);
        }
    }

    private void validateProductDeadline(Product product) {
        if (product.isDeadlineReached()) {
            throw new BusinessException(BIDDING_EXPIRED_PRODUCT);
        }
    }

    private Product findProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(NOT_FOUND));
    }
}
