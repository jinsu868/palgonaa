package com.palgona.palgona.repository.product;

import com.palgona.palgona.common.dto.response.SliceResponse;
import com.palgona.palgona.domain.product.Category;
import com.palgona.palgona.domain.product.SortType;
import com.palgona.palgona.dto.response.ProductPageResponse;
import com.palgona.palgona.repository.product.querydto.ProductDetailQueryResponse;

import java.util.Optional;

public interface ProductRepositoryCustom {

    SliceResponse<ProductPageResponse> findAllByCategoryAndSearchWord(
            Category category, String searchWord, String cursor, SortType sortType, int pageSize);

    Optional<ProductDetailQueryResponse> findProductWithAll(long productId);
}
