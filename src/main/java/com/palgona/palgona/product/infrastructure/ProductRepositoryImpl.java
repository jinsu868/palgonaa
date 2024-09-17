package com.palgona.palgona.product.infrastructure;

import static com.palgona.palgona.bookmark.domain.QBookmark.bookmark;
import static com.palgona.palgona.chat.domain.QChatRoom.chatRoom;
import static com.palgona.palgona.image.domain.QImage.image;
import static com.palgona.palgona.member.domain.QMember.member;
import static com.palgona.palgona.product.domain.QProduct.product;
import static com.palgona.palgona.product.domain.QProductImage.productImage;
import static com.querydsl.core.types.ExpressionUtils.count;

import com.palgona.palgona.common.dto.response.SliceResponse;
import com.palgona.palgona.product.domain.Category;
import com.palgona.palgona.product.domain.ProductState;
import com.palgona.palgona.product.domain.SortType;
import com.palgona.palgona.product.dto.response.ProductPageResponse;
import com.palgona.palgona.product.infrastructure.querydto.ImageQueryResponse;
import com.palgona.palgona.product.infrastructure.querydto.ProductDetailQueryResponse;
import com.palgona.palgona.product.infrastructure.querydto.ProductQueryResponse;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class ProductRepositoryImpl implements ProductRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<ProductDetailQueryResponse> findProductWithAll(long productId){
        return Optional.ofNullable(queryFactory.select(Projections.constructor(
                ProductDetailQueryResponse.class,
                product.id,
                product.name,
                product.content,
                product.category,
                product.productState,
                product.deadline,
                product.createdAt,
                member.id,
                member.nickName,
                member.profileImage,
                product.currentPrice,
                ExpressionUtils.as(
                        JPAExpressions.select(count(bookmark.id))
                                .from(bookmark)
                                .where(bookmark.product.id.eq(productId)),
                        "bookmarkCount"
                ),
                ExpressionUtils.as(
                        JPAExpressions.select(count(chatRoom.id))
                                .from(chatRoom)
                                .where(chatRoom.product.id.eq(productId)),
                        "chatRoomCount"
                )))
                .from(product)
                .join(product.member, member)
                .where(product.id.eq(productId)
                .and(product.productState.ne(ProductState.DELETED))
                )
                .fetchOne());
    }

    @Override
    public SliceResponse<ProductPageResponse> findAllByCategoryAndSearchWord(
            Category category,
            String searchWord,
            String cursor,
            SortType sortType,
            int pageSize
    ) {

        List<ProductQueryResponse> productQueryResponses = queryFactory.select(Projections.constructor(
                ProductQueryResponse.class,
                        product.id,
                        product.name,
                        product.currentPrice,
                        ExpressionUtils.as(
                                JPAExpressions.select(bookmark.count())
                                        .from(bookmark)
                                        .where(bookmark.product.eq(product)),
                                "bookmarkCount"
                        ),
                        product.deadline,
                        product.createdAt
                ))
                .from(product)
                .where(contains(searchWord), categoryEq(category), isInSearchRange(cursor, sortType))
                .orderBy(createOrderSpecifier(sortType))
                .limit(pageSize + 1)
                .setHint("javax.persistence.query.forceIndex", "idx_current_price_id")
                .fetch();

        List<ImageQueryResponse> imageQueryResponses = queryFactory.select(Projections.constructor(
                ImageQueryResponse.class,
                        product.id,
                        image.imageId,
                        image.imageUrl))
                .from(productImage)
                .join(productImage.image, image)
                .where(productImage.product.id.in(toProductIds(productQueryResponses)))
                .fetch();

        Map<Long, List<ImageQueryResponse>> result = imageQueryResponses.stream()
                .collect(Collectors.groupingBy(ImageQueryResponse::productId));

        List<ProductPageResponse> productResponses = productQueryResponses.stream()
                .map(response -> {
                    List<ImageQueryResponse> images = result.get(response.id());
                    Long minId = Long.MAX_VALUE;
                    String imageUrl = null;
                    for (ImageQueryResponse image : images) {
                        if (minId > image.imageId()) {
                            minId = image.imageId();
                            imageUrl = image.imageUrl();
                        }
                    }

                    return ProductPageResponse.of(response, imageUrl);
                }).collect(Collectors.toList());

        return convertToSlice(productResponses, sortType, pageSize);
    }

    private List<Long> toProductIds(List<ProductQueryResponse> productResponses) {
        return productResponses.stream()
                .map(ProductQueryResponse::id)
                .toList();
    }

    private BooleanExpression categoryEq(Category category) {
        if (category != null) {
            return product.category.eq(category);
        }

        return null;
    }

    private BooleanExpression stateIsNotDeleted() {
        return product.productState.ne(ProductState.DELETED);
    }

    private BooleanExpression contains(String searchWord) {
        if (searchWord != null) {
            return product.name.like(searchWord + "%");
        }

        return null;
    }

    private SliceResponse<ProductPageResponse> convertToSlice(
            List<ProductPageResponse> products,
            SortType sortType,
            int pageSize) {
        if (products.isEmpty()) {
            return SliceResponse.of(products, false, null);
        }

        boolean hasNext = existNextPage(products, pageSize);
        if (hasNext) {
            deleteLastPage(products, pageSize);
        }

        String nextCursor = generateCursor(products, sortType);
        return SliceResponse.of(products, hasNext, nextCursor);
    }

    private String generateCursor(List<ProductPageResponse> products, SortType sortType) {
        ProductPageResponse lastProduct = products.get(products.size() - 1);

        return switch (sortType) {
            case DEADLINE -> String.valueOf(lastProduct.deadline());
            case HIGHEST_PRICE, LOWEST_PRICE -> String.format("%09d", lastProduct.currentBid())
                    + String.format("%09d", lastProduct.id());
            case BOOK_MARK -> String.format("%09d", lastProduct.bookmarkCount())
                    + String.format("%09d", lastProduct.id());
            default -> String.valueOf(lastProduct.id());
        };
    }

    private boolean existNextPage(List<ProductPageResponse> products, int pageSize) {
        if (products.size() > pageSize){
            return true;
        }
        return false;
    }

    private void deleteLastPage(List<ProductPageResponse> products, int pageSize) {
        products.remove(pageSize);
    }

    private BooleanExpression isInSearchRange(String cursor, SortType sortType) {
        if (cursor == null) {
            return null;
        }

        if (sortType.equals(SortType.LATEST)) {
            return product.id.lt(Long.valueOf(cursor));
        } else if (sortType.equals(SortType.DEADLINE)) {
            return product.deadline.before(LocalDateTime.parse(cursor, DateTimeFormatter.ISO_DATE_TIME));
        }

        int beforeHighestPrice = Integer.parseInt(cursor.substring(0, 9));
        Long beforeProductId = Long.parseLong(cursor.substring(9, 18));

        if (sortType.equals(SortType.HIGHEST_PRICE)) {
            return product.currentPrice.lt(beforeHighestPrice)
                    .and(product.id.lt(beforeProductId));
        } else {
            return product.currentPrice.gt(beforeHighestPrice)
                    .and(product.id.lt(beforeProductId));
        }
    }

    private OrderSpecifier createOrderSpecifier(SortType sortType) {
        return switch (sortType) {
            case DEADLINE -> new OrderSpecifier<>(Order.DESC, product.deadline);
            case HIGHEST_PRICE -> new OrderSpecifier<>(Order.DESC, product.currentPrice);
            case LOWEST_PRICE -> new OrderSpecifier<>(Order.ASC, product.currentPrice);
            default -> new OrderSpecifier<>(Order.DESC, product.id);
        };
    }
}
