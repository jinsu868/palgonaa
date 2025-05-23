package com.palgona.palgona.product.domain.repository;

import com.palgona.palgona.product.dto.ProductBookmarkCount;
import com.palgona.palgona.product.dto.ProductDateInfo;
import com.palgona.palgona.product.dto.ProductWithBidAmountInfo;
import com.palgona.palgona.product.dto.ProductWithBidInfo;
import java.util.List;

public interface ProductQueryRepository {
    List<ProductBookmarkCount> getAllBookmarkCountsInIds(List<Long> productIds);

    List<ProductDateInfo> findAllProductDateInfo();

    List<ProductWithBidAmountInfo> findWithBidAmountByIdsIn(List<Long> productIds);

    List<ProductWithBidInfo> findProductsToExpireWithBidInfo();
}
