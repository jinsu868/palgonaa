package com.palgona.palgona.service.bidding;

import com.palgona.palgona.common.error.code.BiddingErrorCode;
import com.palgona.palgona.common.error.exception.BusinessException;
import com.palgona.palgona.bidding.domain.Bidding;
import com.palgona.palgona.member.domain.Member;
import com.palgona.palgona.member.domain.Role;
import com.palgona.palgona.member.domain.Status;
import com.palgona.palgona.product.domain.Category;
import com.palgona.palgona.product.domain.Product;
import com.palgona.palgona.product.domain.ProductState;
import com.palgona.palgona.bidding.dto.request.BiddingAttemptRequest;
import com.palgona.palgona.bidding.domain.BiddingRepository;
import com.palgona.palgona.purchase.infrastructure.PurchaseRepository;
import com.palgona.palgona.member.domain.MemberRepository;
import com.palgona.palgona.product.domain.ProductRepository;
import com.palgona.palgona.bidding.application.BiddingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class BiddingServiceTest {

    @Mock
    private BiddingRepository biddingRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private PurchaseRepository purchaseRepository;

    @Mock
    private MemberRepository memberRepository;

    private BiddingService biddingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        biddingService = new BiddingService(biddingRepository, productRepository, purchaseRepository, memberRepository);
    }

    @Test
    void 입찰_시도_성공한다() {
        // given
        int mileage = 1000;
        Status status = Status.ACTIVE;
        String socialId = "1111";
        Role role = Role.USER;
        Member member = Member.of(mileage, status, socialId, role);
        member.updateMileage(1600);

        long productId = 1L;
        String productName = "상품";
        String productContent = "설명";
        int initialPrice = 1000;
        LocalDateTime deadline = LocalDateTime.now().plusHours(2);
        Category category = Category.BOOK;
        ProductState state = ProductState.ON_SALE;

        Product product = Product.builder().name(productName).content(productContent).initialPrice(initialPrice)
                .deadline(deadline).category(category).productState(state).member(member).build();

        int highestPrice = 1300;
        Bidding existingBidding = Bidding.builder().product(product).member(member).price(highestPrice).build();

        int attemptPrice = 1500;
        BiddingAttemptRequest request = new BiddingAttemptRequest(productId, attemptPrice);

        // when
        when(productRepository.findByIdWithPessimisticLock(productId)).thenReturn(Optional.of(product));
        when(biddingRepository.findHighestPriceByProduct(product)).thenReturn(Optional.of(existingBidding.getPrice()));
        when(memberRepository.findByIdWithPessimisticLock(any())).thenReturn(Optional.of(member));
        when(biddingRepository.findHighestPriceByMember(member)).thenReturn(Optional.of(0));

        // then
        assertDoesNotThrow(() -> biddingService.attemptBidding(member, request));
    }

    @Test
    void 이미_지난_상품에_입찰_실패한다() {
        // given
        int mileage = 1000;
        Status status = Status.ACTIVE;
        String socialId = "1111";
        Role role = Role.USER;
        Member member = Member.of(mileage, status, socialId, role);

        long productId = 1L;
        String productName = "상품";
        String productContent = "설명";
        int initialPrice = 1000;
        LocalDateTime deadline = LocalDateTime.now().minusHours(2); // 이미 마감된 상태
        Category category = Category.BOOK;
        ProductState state = ProductState.ON_SALE;
        Product product = Product.builder().name(productName).content(productContent).initialPrice(initialPrice)
                .deadline(deadline).category(category).productState(state).member(member).build();

        int attemptPrice = 1500;
        BiddingAttemptRequest request = new BiddingAttemptRequest(productId, attemptPrice);

        // when
        when(productRepository.findByIdWithPessimisticLock(productId)).thenReturn(Optional.of(product));

        // then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> biddingService.attemptBidding(member, request));
        assertEquals(BiddingErrorCode.BIDDING_EXPIRED_PRODUCT, exception.getErrorCode());
    }

    @Test
    void 더_낮은_가격_입찰_실패한다() {
        // given
        int mileage = 1000;
        Status status = Status.ACTIVE;
        String socialId = "1111";
        Role role = Role.USER;
        Member member = Member.of(mileage, status, socialId, role);

        long productId = 1L;
        String productName = "상품";
        String productContent = "설명";
        int initialPrice = 1000;
        LocalDateTime deadline = LocalDateTime.now().plusHours(2);
        Category category = Category.BOOK;
        ProductState state = ProductState.ON_SALE;
        Product product = Product.builder().name(productName).content(productContent).initialPrice(initialPrice)
                .deadline(deadline).category(category).productState(state).member(member).build();

        int highestPrice = 1500;
        Bidding existingBidding = Bidding.builder().product(product).member(member).price(highestPrice).build();

        int attemptPrice = 1300;
        BiddingAttemptRequest request = new BiddingAttemptRequest(productId, attemptPrice);

        // when
        when(productRepository.findByIdWithPessimisticLock(productId)).thenReturn(Optional.of(product));
        when(biddingRepository.findHighestPriceByProduct(product)).thenReturn(Optional.of(existingBidding.getPrice()));

        // then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> biddingService.attemptBidding(member, request));
        assertEquals(BiddingErrorCode.BIDDING_LOWER_PRICE, exception.getErrorCode());
    }

    @Test
    void 최소_단위보다_작은_입찰_실패한다() {
        // given
        int mileage = 1000;
        Status status = Status.ACTIVE;
        String socialId = "1111";
        Role role = Role.USER;
        Member member = Member.of(mileage, status, socialId, role);

        long productId = 1L;
        String productName = "상품";
        String productContent = "설명";
        int initialPrice = 1000;
        LocalDateTime deadline = LocalDateTime.now().plusHours(2);
        Category category = Category.BOOK;
        ProductState state = ProductState.ON_SALE;
        Product product = Product.builder().name(productName).content(productContent).initialPrice(initialPrice)
                .deadline(deadline).category(category).productState(state).member(member).build();

        int highestPrice = 1500;
        Bidding existingBidding = Bidding.builder().product(product).member(member).price(highestPrice).build();

        int attemptPrice = 1550;
        BiddingAttemptRequest request = new BiddingAttemptRequest(productId, attemptPrice);

        // when
        when(productRepository.findByIdWithPessimisticLock(productId)).thenReturn(Optional.of(product));
        when(biddingRepository.findHighestPriceByProduct(product)).thenReturn(Optional.of(existingBidding.getPrice()));

        // then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> biddingService.attemptBidding(member, request));
        assertEquals(BiddingErrorCode.BIDDING_INSUFFICIENT_BID, exception.getErrorCode());
    }
}
