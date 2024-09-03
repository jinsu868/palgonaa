package com.palgona.palgona.product.infrastructure;

import com.palgona.palgona.common.dto.response.SliceResponse;
import com.palgona.palgona.product.domain.Category;
import com.palgona.palgona.product.domain.SortType;
import com.palgona.palgona.product.dto.response.ProductPageResponse;
import com.palgona.palgona.product.infrastructure.querydto.ProductDetailQueryResponse;

import java.util.Optional;

public interface ProductRepositoryCustom {

    SliceResponse<ProductPageResponse> findAllByCategoryAndSearchWord(
            Category category, String searchWord, String cursor, SortType sortType, int pageSize);

    Optional<ProductDetailQueryResponse> findProductWithAll(long productId);
}
