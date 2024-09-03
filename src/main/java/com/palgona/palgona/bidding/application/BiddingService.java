package com.palgona.palgona.bidding.application;

import static com.palgona.palgona.common.error.code.BiddingErrorCode.BIDDING_EXPIRED_PRODUCT;
import static com.palgona.palgona.common.error.code.BiddingErrorCode.BIDDING_INSUFFICIENT_BID;
import static com.palgona.palgona.common.error.code.BiddingErrorCode.BIDDING_LOWER_PRICE;
import static com.palgona.palgona.common.error.code.MemberErrorCode.MEMBER_NOT_FOUND;
import static com.palgona.palgona.common.error.code.ProductErrorCode.NOT_FOUND;

import com.palgona.palgona.common.error.exception.BusinessException;
import com.palgona.palgona.bidding.domain.Bidding;
import com.palgona.palgona.bidding.domain.BiddingState;
import com.palgona.palgona.member.domain.Member;
import com.palgona.palgona.product.domain.Product;
import com.palgona.palgona.purchase.domain.Purchase;
import com.palgona.palgona.bidding.dto.request.BiddingAttemptRequest;
import com.palgona.palgona.bidding.domain.BiddingRepository;
import com.palgona.palgona.purchase.infrastructure.PurchaseRepository;
import com.palgona.palgona.member.domain.MemberRepository;
import com.palgona.palgona.product.domain.ProductRepository;
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
        Product product = findProductWithPessimisticLock(request);

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

        Member biddingMember = findMemberWithPessimisticLock(member);
        int previousBid = biddingRepository.findHighestPriceByMember(biddingMember).orElse(0);
        int extraCost = attemptPrice - previousBid;
        biddingMember.useMileage(extraCost);
        Bidding bidding = Bidding.builder().member(biddingMember).product(product).price(attemptPrice).build();

        biddingRepository.save(bidding);
    }

    private Product findProductWithPessimisticLock(BiddingAttemptRequest request) {
        return productRepository.findByIdWithPessimisticLock(request.productId())
                .orElseThrow(() -> new BusinessException(NOT_FOUND));

    }

    public Page<Bidding> findAllByProductId(long productId, Pageable pageable) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 product가 없습니다."));

        return biddingRepository.findAllByProduct(pageable, product);
    }

    @Transactional
    public void checkBiddingExpiration() {
        List<Bidding> expiredBiddings = biddingRepository.findExpiredBiddingsWithPessimisticLock(LocalDateTime.now());

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
            Member biddingMember = bidding.getMember();
            int bidPrice = bidding.getPrice();
            if (bidding.getId().equals(highestPriceBidding.getId())) {
                bidding.updateState(BiddingState.SUCCESS);
                Purchase purchase = Purchase.builder()
                        .bidding(bidding)
                        .member(biddingMember)
                        .purchasePrice(bidPrice)
                        .build();

                purchaseRepository.save(purchase);
            } else {
                bidding.updateState(BiddingState.FAILED);
                if (memberHighestBids.get(biddingMember) == bidPrice) {
                    biddingMember.refundMileage(bidPrice);
                }
            }
        }
    }

    private Member findMemberWithPessimisticLock(Member member) {
        return memberRepository.findByIdWithPessimisticLock(member.getId())
                .orElseThrow(() -> new BusinessException(MEMBER_NOT_FOUND));
    }
}
