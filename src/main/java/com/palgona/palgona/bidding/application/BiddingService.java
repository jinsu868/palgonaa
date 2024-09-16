package com.palgona.palgona.bidding.application;

import static com.palgona.palgona.common.error.code.BiddingErrorCode.BIDDING_EXPIRED_PRODUCT;
import static com.palgona.palgona.common.error.code.BiddingErrorCode.BIDDING_INSUFFICIENT_BID;
import static com.palgona.palgona.common.error.code.BiddingErrorCode.BIDDING_LOWER_PRICE;
import static com.palgona.palgona.common.error.code.BiddingErrorCode.DUPLICATE_HIGHEST_BIDDING_MEMBER;
import static com.palgona.palgona.common.error.code.MemberErrorCode.MEMBER_NOT_FOUND;
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
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
    public void attemptBidding(
            Long productId,
            Member biddingMember,
            int attemptPrice
    ) {
        Product product = findProduct(productId);
        biddingMember = findMemberByIdWithPessimisticLock(biddingMember.getId());

        validateProductDeadline(product);
        validateBiddingCreate(attemptPrice, product, biddingMember);

        int previousBid = product.getCurrentPrice();
        int extraCost = attemptPrice - previousBid;

        biddingMember.useMileage(extraCost);
        product.updatePrice(attemptPrice);
        Bidding bidding = Bidding.builder()
                .member(biddingMember)
                .product(product)
                .price(attemptPrice)
                .build();

        biddingRepository.save(bidding);
    }

    private Member findMemberByIdWithPessimisticLock(Long id) {
        return memberRepository.findByIdWithPessimisticLock(id)
                        .orElseThrow(() -> new BusinessException(MEMBER_NOT_FOUND));
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
            Set<Member> biddingMembers = new HashSet<>();

            if (biddings.isEmpty()) {
                expiredProduct.expire();
                continue;
            }

            expiredProduct.sell();
            Bidding latestBidding = biddings.get(0);
            latestBidding.success();
            biddingMembers.add(latestBidding.getMember());

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
                if (biddingMembers.contains(losingMember)) {
                    continue;
                }

                losingMember = findMemberByIdWithPessimisticLock(losingMember.getId());
                losingMember.refundMileage(failedBidding.getPrice());
                failedBidding.fail();
                biddingMembers.add(losingMember);
            }
        }
    }

    private void validateBiddingCreate(
            int attemptPrice,
            Product product,
            Member biddingMember
    ) {
        Optional<Bidding> highestBidding = biddingRepository.findHighestPriceBiddingByProduct(product);
        int highestPrice = product.getCurrentPrice();

        if (highestBidding.isPresent()) {
            validateDuplicateBidding(highestBidding.get(), biddingMember);
        }

        int threshold = (int) Math.pow(10, String.valueOf(attemptPrice).length() - 2);
        int priceDifference = attemptPrice -  highestPrice;

        if (priceDifference >= attemptPrice) {
            throw new BusinessException(BIDDING_LOWER_PRICE);
        }

        if (priceDifference < threshold) {
            throw new BusinessException(BIDDING_INSUFFICIENT_BID);
        }
    }

    private void validateDuplicateBidding(
            Bidding highestBidding,
            Member biddingMember
    ) {
        if (highestBidding.isOwner(biddingMember)) {
            throw new BusinessException(DUPLICATE_HIGHEST_BIDDING_MEMBER);
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
