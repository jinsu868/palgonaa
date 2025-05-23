package com.palgona.palgona.purchase.domain.repository;

import static com.palgona.palgona.bidding.domain.QBid.bid;
import static com.palgona.palgona.product.domain.QProduct.product;
import static com.palgona.palgona.purchase.domain.QPurchase.purchase;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.palgona.palgona.purchase.domain.PurchaseState;
import com.palgona.palgona.purchase.dto.response.PurchaseResponse;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class PurchaseQueryRepositoryImpl implements PurchaseQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<PurchaseResponse> findAllByUserId(
            Long userId,
            String pageToken,
            PurchaseState state,
            int pageSize
    ) {
        return queryFactory.select(Projections.constructor(
                PurchaseResponse.class,
                purchase.id,
                purchase.productId,
                bid.amount,
                product.name,
                purchase.state
        )).from(purchase)
                .innerJoin(product).on(product.id.eq(purchase.productId))
                .innerJoin(bid).on(bid.productId.eq(product.id))
                .where(isInState(state),
                        purchase.userId.eq(userId),
                        isInRange(pageToken))
                .limit(pageSize + 1)
                .orderBy(purchase.id.desc())
                .fetch();
    }

    private BooleanExpression isInRange(String pageToken) {
        if (pageToken == null) {
            return null;
        }

        return purchase.id.lt(Long.valueOf(pageToken));
    }

    private BooleanExpression isInState(PurchaseState state) {
        if (state == null) {
            return null;
        }

        return purchase.state.eq(state);
    }
}
