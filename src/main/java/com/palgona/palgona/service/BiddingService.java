package com.palgona.palgona.service;

import static com.palgona.palgona.common.error.code.BiddingErrorCode.BIDDING_EXPIRED_PRODUCT;
import static com.palgona.palgona.common.error.code.BiddingErrorCode.BIDDING_INSUFFICIENT_BID;
import static com.palgona.palgona.common.error.code.BiddingErrorCode.BIDDING_LOWER_PRICE;
import static com.palgona.palgona.common.error.code.MemberErrorCode.MEMBER_NOT_FOUND;

import com.palgona.palgona.common.error.exception.BusinessException;
import com.palgona.palgona.domain.bidding.Bidding;
import com.palgona.palgona.domain.bidding.BiddingState;
import com.palgona.palgona.domain.member.Member;
import com.palgona.palgona.domain.product.Product;
import com.palgona.palgona.domain.purchase.Purchase;
import com.palgona.palgona.dto.BiddingAttemptRequest;
import com.palgona.palgona.repository.BiddingRepository;
import com.palgona.palgona.repository.purchase.PurchaseRepository;
import com.palgona.palgona.repository.member.MemberRepository;
import com.palgona.palgona.repository.product.ProductRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    @Transactional
    public void attemptBidding(Member member, BiddingAttemptRequest request) {
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 product가 없습니다."));

        int attemptPrice = request.price();

        if (product.isDeadlineReached()) {
            throw new BusinessException(BIDDING_EXPIRED_PRODUCT);
        }

        int highestPrice = biddingRepository.findHighestPriceByProduct(product).orElse(0);

        if (attemptPrice <= highestPrice) {
            throw new BusinessException(BIDDING_LOWER_PRICE);
        }

        int threshold = (int) Math.pow(10, String.valueOf(attemptPrice).length() - 2);
        int priceDifference = attemptPrice - highestPrice;

        if (priceDifference < threshold) {
            throw new BusinessException(BIDDING_INSUFFICIENT_BID);
        }
        
        Bidding bidding = Bidding.builder().member(member).product(product).price(attemptPrice).build();

        biddingRepository.save(bidding);
    }

    public Page<Bidding> findAllByProductId(long productId, Pageable pageable) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 product가 없습니다."));

        return biddingRepository.findAllByProduct(pageable, product);
    }

    @Transactional
    public void checkBiddingExpiration() {
        List<Bidding> expiredBiddings = biddingRepository.findExpiredBiddings(LocalDateTime.now());

        Map<Long, Bidding> highestPriceBiddings = new HashMap<>();
        Map<Member, Integer> memberHighestBids = new HashMap<>();

        for (Bidding bidding : expiredBiddings) {
            Long productId = bidding.getProduct().getId();
            highestPriceBiddings.compute(productId, (key, oldValue) -> {
                if (oldValue == null || bidding.getPrice() > oldValue.getPrice() || (
                        bidding.getPrice() == oldValue.getPrice() && bidding.getUpdatedAt()
                                .isBefore(oldValue.getUpdatedAt()))) {
                    return bidding;
                }

                return oldValue;
            });

            memberHighestBids.compute(bidding.getMember(), (member, currentBid) -> {
                if (currentBid == null || currentBid < bidding.getPrice()) {
                    return bidding.getPrice();
                }

                return currentBid;
            });
        }

        for (Bidding bidding : expiredBiddings) {
            Long productId = bidding.getProduct().getId();
            Bidding highestPriceBidding = highestPriceBiddings.get(productId);
            if (bidding.getId().equals(highestPriceBidding.getId())) {
                bidding.updateState(BiddingState.SUCCESS);
                Purchase purchase = Purchase.builder()
                        .bidding(bidding)
                        .member(bidding.getMember())
                        .purchasePrice(bidding.getPrice())
                        .build();

                purchaseRepository.save(purchase);
            } else {
                bidding.updateState(BiddingState.FAILED);
                if (memberHighestBids.get(bidding.getMember()) == bidding.getPrice()) {
                    bidding.getMember().refundMileage(bidding.getPrice());
                }
            }
        }
    }
}
