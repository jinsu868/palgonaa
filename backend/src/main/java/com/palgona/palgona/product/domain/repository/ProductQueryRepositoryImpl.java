package com.palgona.palgona.product.domain.repository;

import static com.palgona.palgona.bidding.domain.QBid.bid;
import static com.palgona.palgona.product.domain.QBookmark.bookmark;
import static com.palgona.palgona.product.domain.QProduct.product;
import static com.palgona.palgona.user.domain.QUser.user;

import com.palgona.palgona.product.domain.ProductState;
import com.palgona.palgona.product.dto.ProductWithBidAmountInfo;
import com.palgona.palgona.product.dto.ProductWithBidInfo;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.palgona.palgona.product.dto.ProductBookmarkCount;
import com.palgona.palgona.product.dto.ProductDateInfo;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ProductQueryRepositoryImpl implements ProductQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<ProductBookmarkCount> getAllBookmarkCountsInIds(List<Long> productIds) {
        return queryFactory.select(Projections.constructor(
                ProductBookmarkCount.class,
                bookmark.productId,
                bookmark.count().intValue()
        )).from(bookmark)
                .where(bookmark.productId.in(productIds))
                .groupBy(bookmark.productId)
                .fetch();
    }

    @Override
    public List<ProductDateInfo> findAllProductDateInfo() {
        return queryFactory.select(Projections.constructor(
                ProductDateInfo.class,
                product.id,
                product.createdAt
        )).from(product)
                .fetch();
    }

    @Override
    public List<ProductWithBidAmountInfo> findWithBidAmountByIdsIn(List<Long> productIds) {
        return queryFactory.select(Projections.constructor(
                ProductWithBidAmountInfo.class,
                product.id,
                product.name,
                bid.amount.intValue(),
                product.deadline,
                product.imageUrls,
                user.nickname
        )).from(product)
                .leftJoin(user).on(user.id.eq(product.userId))
                .leftJoin(bid).on(bid.productId.eq(product.id))
                .where(product.id.in(productIds))
                .fetch();
    }

    @Override
    public List<ProductWithBidInfo> findProductsToExpireWithBidInfo() {
        return queryFactory.select(Projections.constructor(
                ProductWithBidInfo.class,
                product.id,
                bid.userId,
                bid.amount
        )).from(product)
                .leftJoin(bid).on(bid.productId.eq(product.id))
                .where(product.state.eq(ProductState.ON_SALE),
                        product.deadline.loe(LocalDateTime.now()))
                .fetch();
    }
}
