package com.palgona.palgona.product.domain;

import lombok.Builder;
import lombok.Getter;

/**
 * Product 의 MetaData 저장 Table
 * score : bookmarkCount * (0.95)^(날짜)
 * 시간이 지날수록 점수가 떨어지고 bookmark 개수가 고려된 수치
 * => 3시간 주기로 Batch 를 통해 Update
 */

@Getter
public class ProductMeta {

    private final Long productId;
    private final int bookmarkCount;
    private final double score;

    @Builder
    public ProductMeta(Long productId, int bookmarkCount, double score) {
        this.productId = productId;
        this.bookmarkCount = bookmarkCount;
        this.score = score;
    }
}
