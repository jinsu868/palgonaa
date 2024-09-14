package com.palgona.palgona.purchase.infrastructure;

import static com.palgona.palgona.bidding.domain.QBidding.bidding;
import static com.palgona.palgona.image.domain.QImage.image;
import static com.palgona.palgona.product.domain.QProduct.product;
import static com.palgona.palgona.product.domain.QProductImage.productImage;
import static com.palgona.palgona.purchase.domain.QPurchase.purchase;

import com.palgona.palgona.common.dto.response.SliceResponse;
import com.palgona.palgona.member.domain.Member;
import com.palgona.palgona.purchase.dto.response.PurchaseResponse;
import com.palgona.palgona.product.infrastructure.querydto.ImageQueryResponse;
import com.palgona.palgona.purchase.infrastructure.queryDto.PurchaseQueryResponse;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class PurchaseRepositoryImpl implements PurchaseRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public SliceResponse<PurchaseResponse> findAllByMember(Member member, int pageSize, String cursor) {
        List<PurchaseQueryResponse> purchaseQueryResponses = queryFactory.select(Projections.constructor(
                        PurchaseQueryResponse.class,
                        purchase.id,
                        product.id,
                        purchase.purchasePrice,
                        product.category,
                        purchase.state,
                        purchase.reason,
                        product.name
                ))
                .from(purchase)
                .innerJoin(purchase.bidding, bidding)
                .innerJoin(bidding.product, product)
                .where(purchase.buyer.eq(member),
                        ltPurchaseId(cursor))
                .orderBy(purchase.id.desc())
                .limit(pageSize + 1)
                .fetch();

        List<ImageQueryResponse> imageQueryResponses = queryFactory.select(Projections.constructor(
                        ImageQueryResponse.class,
                        product.id,
                        image.imageId,
                        image.imageUrl))
                .from(productImage)
                .join(productImage.image, image)
                .where(productImage.product.id.in(toProductIds(purchaseQueryResponses)))
                .fetch();

        Map<Long, List<ImageQueryResponse>> result = imageQueryResponses.stream()
                .collect(Collectors.groupingBy(ImageQueryResponse::productId));

        List<PurchaseResponse> purchaseResponses = purchaseQueryResponses.stream()
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

                    return PurchaseResponse.of(response, imageUrl);
                }).collect(Collectors.toList());

        return convertToSlice(purchaseResponses, pageSize);
    }

    private SliceResponse<PurchaseResponse> convertToSlice(List<PurchaseResponse> purchases, int pageSize) {
        if (purchases.isEmpty()) {
            return SliceResponse.of(purchases, false, null);
        }

        boolean hasNext = existNextPage(purchases, pageSize);
        if (hasNext) {
            deleteLastPage(purchases, pageSize);
        }

        String nextCursor = String.valueOf(purchases.get(purchases.size() - 1));
        return SliceResponse.of(purchases, hasNext, nextCursor);
    }

    private boolean existNextPage(List<PurchaseResponse> purchases, int pageSize) {
        if (purchases.size() > pageSize) {
            return true;
        }

        return false;
    }

    private void deleteLastPage(List<PurchaseResponse> purchases, int pageSize) {
        purchases.remove(pageSize);
    }

    private BooleanExpression ltPurchaseId(String cursor) {
        if (cursor != null) {
            return purchase.id.lt(Long.valueOf(cursor));
        }

        return null;
    }

    private List<Long> toProductIds(List<PurchaseQueryResponse> purchaseResponses) {
        return purchaseResponses.stream()
                .map(PurchaseQueryResponse::id)
                .toList();
    }
}
