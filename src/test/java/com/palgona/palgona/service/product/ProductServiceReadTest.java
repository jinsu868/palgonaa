//package com.palgona.palgona.service.product;
//
//import com.palgona.palgona.common.dto.CustomMemberDetails;
//import com.palgona.palgona.common.error.code.ProductErrorCode;
//import com.palgona.palgona.common.error.exception.BusinessException;
//import com.palgona.palgona.member.domain.Member;
//import com.palgona.palgona.member.domain.Role;
//import com.palgona.palgona.member.domain.Status;
//import com.palgona.palgona.product.domain.Category;
//import com.palgona.palgona.product.domain.Product;
//import com.palgona.palgona.product.domain.ProductState;
//import com.palgona.palgona.product.dto.response.ProductDetailResponse;
//import com.palgona.palgona.product.domain.ProductImageRepository;
//import com.palgona.palgona.notification.domain.SilentNotificationsRepository;
//import com.palgona.palgona.product.domain.ProductRepository;
//import com.palgona.palgona.repository.product.querydto.ProductDetailQueryResponse;
//import com.palgona.palgona.product.application.ProductService;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.time.LocalDateTime;
//import java.util.Arrays;
//import java.util.List;
//import java.util.Optional;
//
//import static com.palgona.palgona.common.error.code.ProductErrorCode.NOT_FOUND;
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.when;
//
//@ExtendWith(MockitoExtension.class)
//public class ProductServiceReadTest {
//    @Mock
//    private ProductRepository productRepository;
//    @Mock
//    private ProductImageRepository productImageRepository;
//    @Mock
//    private SilentNotificationsRepository silentNotificationsRepository;
//    @InjectMocks
//    private ProductService productService;
//
//    @Test
//    void 상품_상세_조회_성공() {
//        // given
//        // 멤버
//        Member member = createMember();
//        CustomMemberDetails memberDetails = new CustomMemberDetails(member);
//
//        // 상품
//        Long productId = 1L;
//        String productName = "상품1";
//        String content = "이것은 상품 설명 부분";
//        String category = Category.BOOK.getKey();
//        String productState = ProductState.ON_SALE.getKey();
//        LocalDateTime deadline = LocalDateTime.now().plusDays(3);
//        LocalDateTime create_at = LocalDateTime.now();
//        Long ownerId = 1L;
//        String ownerName = "상품 주인";
//        String ownerImgUrl = "/profile";
//        Integer highestPrice = 10000;
//        Integer bookmarkCount = 3;
//
//        Product product = Product.builder()
//                .name(productName)
//                .content(content)
//                .category(Category.valueOf(category))
//                .productState(ProductState.valueOf(productState))
//                .deadline(deadline)
//                .build();
//
//        ProductDetailQueryResponse response = new ProductDetailQueryResponse(
//                product,
//                ownerId,
//                ownerName,
//                ownerImgUrl,
//                highestPrice,
//                bookmarkCount
//        );
//
//        List<String> imageUrls = Arrays.asList("image1", "image2");
//
//        // when
//        when(productRepository.findProductWithAll(productId)).thenReturn(Optional.of(response));
//        when(productImageRepository.findProductImageUrlsByProduct(productId)).thenReturn(imageUrls);
//        when(silentNotificationsRepository.findByMemberAndProduct(member, product)).thenReturn(Optional.empty());
//        ProductDetailResponse result = productService.readProduct(productId, memberDetails);
//
//        // then
//        assertThat(result.productName()).isEqualTo(productName);
//        assertThat(result.content()).isEqualTo(content);
//        assertThat(result.category()).isEqualTo(category);
//        assertThat(result.productState()).isEqualTo(productState);
//        assertThat(result.deadline()).isEqualTo(deadline);
////        assertThat(result.created_at()).isEqualTo(create_at); baseEntity값은 어떻게 테스트 하지?
//        assertThat(result.ownerId()).isEqualTo(ownerId);
//        assertThat(result.ownerName()).isEqualTo(ownerName);
//        assertThat(result.ownerImgUrl()).isEqualTo(ownerImgUrl);
//        assertThat(result.highestPrice()).isEqualTo(highestPrice);
//        assertThat(result.bookmarkCount()).isEqualTo(bookmarkCount);
//        assertThat(result.imageUrls()).isEqualTo(imageUrls);
//        assertThat(result.isSilent()).isEqualTo(false);
//    }
//
//    @Test
//    void 실패_유효하지_않은_상품id() {
//        // given
//        // 멤버
//        Member member = createMember();
//        CustomMemberDetails memberDetails = new CustomMemberDetails(member);
//
//        Long productId = 2L;
//
//        // when
//        when(productRepository.findProductWithAll(productId)).thenReturn(Optional.empty());
//
//        // then
//        BusinessException exception = assertThrows(BusinessException.class,
//                () -> productService.readProduct(productId, memberDetails));
//
//        assertThat(exception.getErrorCode()).isEqualTo(ProductErrorCode.NOT_FOUND);
//    }
//
//    @Test
//    void 실패_삭제된_상품(){
//        //given
//        // 멤버
//        Member member = createMember();
//        CustomMemberDetails memberDetails = new CustomMemberDetails(member);
//
//        //상품
//        Long productId = 1L;
//        String productName = "상품1";
//        String content = "이것은 상품 설명 부분";
//        String category = Category.BOOK.getKey();
//        String productState = ProductState.DELETED.getKey(); //삭제된 상품
//        LocalDateTime deadline = LocalDateTime.now().plusDays(3);
//        LocalDateTime create_at = LocalDateTime.now();
//        Long ownerId = 1L;
//        String ownerName = "상품 주인";
//        String ownerImgUrl = "/profile";
//        Integer highestPrice = 10000;
//        Integer bookmarkCount = 3;
//
//        Product product = Product.builder()
//                .name(productName)
//                .content(content)
//                .category(Category.valueOf(category))
//                .productState(ProductState.valueOf(productState))
//                .deadline(deadline)
//                .build();
//
//        ProductDetailQueryResponse response = new ProductDetailQueryResponse(
//                product,
//                ownerId,
//                ownerName,
//                ownerImgUrl,
//                highestPrice,
//                bookmarkCount
//        );
//
//        // when
//        when(productRepository.findProductWithAll(productId)).thenReturn(Optional.of(response));
//        BusinessException exception = assertThrows(BusinessException.class,
//                () -> productService.readProduct(productId, memberDetails));
//
//        // then
//        assertThat(exception.getErrorCode()).isEqualTo(ProductErrorCode.DELETED_PRODUCT);
//    }
//
//    private Member createMember(){
//        int mileage = 1000;
//        Status status = Status.ACTIVE;
//        String socialId = "1111";
//        Role role = Role.USER;
//        Member member = Member.of(mileage, status, socialId, role);
//
//        return member;
//    }
//
//}
