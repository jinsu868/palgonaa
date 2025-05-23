package com.palgona.palgona.product.domain.repository;

import com.palgona.palgona.product.domain.ProductMeta;
import com.palgona.palgona.product.dto.ProductBookmarkCount;
import com.palgona.palgona.product.dto.ProductDateInfo;
import com.palgona.palgona.product.dto.ProductStatisticInfo;
import com.palgona.palgona.product.dto.ProductWithBidAmountInfo;
import com.palgona.palgona.product.dto.ProductWithBidInfo;
import com.palgona.palgona.product.dto.response.ProductDetailResponse;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.palgona.palgona.product.domain.Product;
import com.palgona.palgona.product.domain.ProductCategory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ProductRepository {

    private static final int DEFAULT_PAGE_SIZE = 10;

    private final ProductJpaRepository productJpaRepository;
    private final ProductJdbcRepository productJdbcRepository;

    public Product save(Product product) {
        return productJpaRepository.save(product);
    }

    public Optional<Product> findById(Long productId) {
        return productJpaRepository.findById(productId);
    }

    public List<Product> findAllOngoing() {
        return productJpaRepository.findAllOngoing();
    }

    public List<ProductWithBidInfo> findProductsToExpireWithBidAmount() {
        return productJpaRepository.findProductsToExpireWithBidInfo();
    }

    public void updateToExpireInProductIds(List<Long> productIds) {
        productJdbcRepository.updateProductsToExpireInIds(productIds);
    }

    public void updateProduct(
            Long productId,
            List<String> imageUrls,
            ProductCategory category,
            String name,
            String content
    ) {
        productJdbcRepository.update(productId, imageUrls, category, name, content);
    }

    public boolean existsById(Long productId) {
        return productJpaRepository.existsById(productId);
    }

    public void delete(Product product) {
        productJpaRepository.delete(product);
    }


    public List<ProductDateInfo> findAllProductDate() {
        return productJpaRepository.findAllProductDateInfo();
    }

    public void updateScores(List<ProductStatisticInfo> productStatisticInfos) {
        productJdbcRepository.batchUpdate(productStatisticInfos);
    }

    public List<ProductMeta> findProductMetasWithPaging(String pageToken) {
        return productJdbcRepository.findProductMetaInfoWithPaging(pageToken, DEFAULT_PAGE_SIZE);
    }

    public List<ProductWithBidAmountInfo> findWithBidAmountByIdsIn(List<Long> productIds) {
        return productJpaRepository.findWithBidAmountByIdsIn(productIds);
    }

    public Optional<ProductDetailResponse> findDetailById(Long productId) {
        return productJdbcRepository.findDetailProductInfo(productId);
    }

    public void insertProductMeta(ProductMeta productMeta) {
        productJdbcRepository.insert(productMeta);
    }

    public List<ProductBookmarkCount> getAllBookmarkCountsInIds(List<Long> productIds) {
        return productJpaRepository.getAllBookmarkCountsInIds(productIds);
    }
}
