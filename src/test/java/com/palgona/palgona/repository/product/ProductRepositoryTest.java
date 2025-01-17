package com.palgona.palgona.repository.product;

import static org.assertj.core.api.Assertions.assertThat;

import com.palgona.palgona.common.RepositoryTest;
import com.palgona.palgona.common.dto.response.SliceResponse;
import com.palgona.palgona.bidding.domain.Bidding;
import com.palgona.palgona.bookmark.domain.Bookmark;
import com.palgona.palgona.image.domain.Image;
import com.palgona.palgona.member.domain.Member;
import com.palgona.palgona.member.domain.Role;
import com.palgona.palgona.member.domain.Status;
import com.palgona.palgona.product.domain.Category;
import com.palgona.palgona.product.domain.Product;
import com.palgona.palgona.product.domain.ProductImage;
import com.palgona.palgona.product.domain.ProductRepository;
import com.palgona.palgona.product.domain.ProductState;
import com.palgona.palgona.product.domain.SortType;
import com.palgona.palgona.product.dto.response.ProductPageResponse;
import com.palgona.palgona.bidding.domain.BiddingRepository;
import com.palgona.palgona.bookmark.domain.BookmarkRepository;
import com.palgona.palgona.image.domain.ImageRepository;
import com.palgona.palgona.product.domain.ProductImageRepository;
import com.palgona.palgona.member.domain.MemberRepository;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;

@RepositoryTest
class ProductRepositoryTest {

    @Autowired
    ProductRepository productRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    ImageRepository imageRepository;

    @Autowired
    ProductImageRepository productImageRepository;

    @Autowired
    BookmarkRepository bookmarkRepository;

    @Autowired
    BiddingRepository biddingRepository;

    @BeforeEach
    void setUp() {
        Member member = memberRepository.save(Member.of(0, Status.ACTIVE, "100", Role.USER));
        List<String> categories = Arrays.asList("DIGITAL_DEVICE", "FURNITURE", "CLOTHING", "FOOD", "BOOK");
        for (int i = 1; i <= 5; i++) {
            Product product = Product.builder()
                    .productState(ProductState.ON_SALE)
                    .content("qwer")
                    .deadline(LocalDateTime.now())
                    .initialPrice(1000)
                    .category(Category.from(categories.get(i-1)))
                    .member(member)
                    .name("qwerasdf")
                    .build();

            product = productRepository.save(product);

            for (int j = 0; j < i; j++) {
                Bookmark bookmark = Bookmark.builder()
                        .product(product)
                        .member(member)
                        .build();

                bookmarkRepository.save(bookmark);
            }

            for (int j = 0; j < i; j++) {
                Bidding bidding = Bidding.builder()
                        .product(product)
                        .member(member)
                        .price(1000 * j)
                        .build();

                biddingRepository.save(bidding);
            }

            Image image1 = Image.builder()
                    .imageUrl("qwer.png")
                    .build();
            Image image2 = Image.builder()
                    .imageUrl("asdf.png")
                    .build();

            image1 = imageRepository.save(image1);
            image2 = imageRepository.save(image2);

            ProductImage productImage1 = ProductImage.builder()
                    .product(product)
                    .image(image1)
                    .build();

            ProductImage productImage2 = ProductImage.builder()
                    .product(product)
                    .image(image2)
                    .build();

            product.addProductImage(productImage1);
            product.addProductImage(productImage2);
            productImageRepository.save(productImage1);
            productImageRepository.save(productImage2);
        }
    }

    @Test
    void 최신순으로_상품_조회() {

        SliceResponse<ProductPageResponse> response1 = productRepository.findAllByCategoryAndSearchWord(
                null, null, null, SortType.LATEST, 3);

        assertThat(response1.hasNext()).isTrue();
        assertThat(response1.values().size()).isEqualTo(3);
        assertThat(response1.values().get(0).id()).isGreaterThan(response1.values().get(1).id());

        String cursor = response1.cursor();

        SliceResponse<ProductPageResponse> response2 = productRepository.findAllByCategoryAndSearchWord(
                null, null, cursor, SortType.LATEST, 3);

        assertThat(response2.hasNext()).isFalse();
        assertThat(response2.values().size()).isEqualTo(2);
        assertThat(response2.values().get(0).id()).isGreaterThan(response2.values().get(1).id());
    }

    @Test
    void 데드라인순으로_상품_조회() {
        SliceResponse<ProductPageResponse> response1 = productRepository.findAllByCategoryAndSearchWord(
                null, null, null, SortType.DEADLINE, 3);

        assertThat(response1.hasNext()).isTrue();
        assertThat(response1.values().size()).isEqualTo(3);
        assertThat(response1.values().get(1).deadline()).isBefore(response1.values().get(0).deadline());

        String cursor = response1.cursor();

        SliceResponse<ProductPageResponse> response2 = productRepository.findAllByCategoryAndSearchWord(
                null, null, cursor, SortType.DEADLINE, 3);

        assertThat(response2.hasNext()).isFalse();
        assertThat(response2.values().size()).isEqualTo(2);
        assertThat(response2.values().get(1).deadline()).isBefore(response2.values().get(0).deadline());
    }

    @Test
    void 높은_가격순으로_상품_조회() {
        SliceResponse<ProductPageResponse> response1 = productRepository.findAllByCategoryAndSearchWord(
                null, null, null, SortType.HIGHEST_PRICE, 3);

        assertThat(response1.hasNext()).isTrue();
        assertThat(response1.values().size()).isEqualTo(3);
        assertThat(response1.values().get(0).currentBid()).isEqualTo(4000);

        String cursor = response1.cursor();

        SliceResponse<ProductPageResponse> response2 = productRepository.findAllByCategoryAndSearchWord(
                null, null, cursor, SortType.HIGHEST_PRICE, 3);

        assertThat(response2.hasNext()).isFalse();
        assertThat(response2.values().size()).isEqualTo(2);
        assertThat(response2.values().get(0).currentBid()).isEqualTo(1000);
        assertThat(response2.values().get(1).currentBid()).isEqualTo(0);
    }

    @Test
    void 낮은_가격순으로_상품_조회() {
        SliceResponse<ProductPageResponse> response1 = productRepository.findAllByCategoryAndSearchWord(
                null, null, null, SortType.LOWEST_PRICE, 3);

        assertThat(response1.hasNext()).isTrue();
        assertThat(response1.values().size()).isEqualTo(3);
        assertThat(response1.values().get(0).currentBid()).isEqualTo(0);

        String cursor = response1.cursor();

        SliceResponse<ProductPageResponse> response2 = productRepository.findAllByCategoryAndSearchWord(
                null, null, cursor, SortType.LOWEST_PRICE, 3);

        assertThat(response2.hasNext()).isFalse();
        assertThat(response2.values().size()).isEqualTo(2);
        assertThat(response2.values().get(0).currentBid()).isEqualTo(3000);
        assertThat(response2.values().get(1).currentBid()).isEqualTo(4000);
    }

    @Test
    void 북마크순으로_상품_조회() {
        SliceResponse<ProductPageResponse> response1 = productRepository.findAllByCategoryAndSearchWord(
                null, null, null, SortType.BOOK_MARK, 3);

        assertThat(response1.hasNext()).isTrue();
        assertThat(response1.values().size()).isEqualTo(3);
        assertThat(response1.values().get(0).bookmarkCount()).isEqualTo(5);

        String cursor = response1.cursor();

        SliceResponse<ProductPageResponse> response2 = productRepository.findAllByCategoryAndSearchWord(
                null, null, cursor, SortType.BOOK_MARK, 3);

        assertThat(response2.hasNext()).isFalse();
        assertThat(response2.values().size()).isEqualTo(2);
        assertThat(response2.values().get(0).bookmarkCount()).isEqualTo(2);
        assertThat(response2.values().get(1).bookmarkCount()).isEqualTo(1);
    }

    @ParameterizedTest
    @ValueSource(strings = {"we", "wer", "asd"})
    void 검색어가_제목_가운데_포함되는_경우(String word) {
        SliceResponse<ProductPageResponse> response = productRepository.findAllByCategoryAndSearchWord(
                null, word, null, SortType.LATEST, 5);

        assertThat(response.values().size()).isEqualTo(5);
    }

    @ParameterizedTest
    @ValueSource(strings = {"q", "qw", "f", "df"})
    void 검색어로_시작하거나_검색어로_끝나는_경우(String word) {
        SliceResponse<ProductPageResponse> response = productRepository.findAllByCategoryAndSearchWord(
                null, word, null, SortType.LATEST, 5);

        assertThat(response.values().size()).isEqualTo(5);
    }

    @ParameterizedTest
    @ValueSource(strings = {"zx", "xc", "vb"})
    void 검색어가_포함되지_않는_경우(String word) {
        SliceResponse<ProductPageResponse> response = productRepository.findAllByCategoryAndSearchWord(
                null, word, null, SortType.LATEST, 5);

        assertThat(response.values().size()).isEqualTo(0);
    }

    @ParameterizedTest
    @ValueSource(strings = {"DIGITAL_DEVICE", "FURNITURE", "CLOTHING", "FOOD", "BOOK"})
    void 카테고리에_포함된_상품_조회(String source) {
        Category category = Category.from(source);
        SliceResponse<ProductPageResponse> response = productRepository.findAllByCategoryAndSearchWord(
                category, null, null, SortType.LATEST, 5);

        assertThat(response.values().size()).isEqualTo(1);
    }

}