package com.palgona.palgona.product.application;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import com.palgona.palgona.product.dto.response.ProductResponse;
import com.palgona.palgona.product.domain.ProductMeta;
import com.palgona.palgona.product.domain.Product;
import com.palgona.palgona.product.domain.ProductState;
import com.palgona.palgona.product.dto.request.ProductCreateRequest;
import com.palgona.palgona.product.dto.response.ProductDetailResponse;
import com.palgona.palgona.product.dto.ProductWithBidAmountInfo;
import com.palgona.palgona.user.domain.User;
import com.palgona.palgona.common.error.BadRequestException;
import com.palgona.palgona.common.error.ErrorCode;
import com.palgona.palgona.common.model.PageInfo;
import com.palgona.palgona.product.domain.repository.ProductRepository;
import com.palgona.palgona.product.dto.request.ProductUpdateRequest;
import com.palgona.palgona.product.dto.ProductBookmarkCount;
import com.palgona.palgona.product.dto.ProductDateInfo;
import com.palgona.palgona.product.dto.ProductStatisticInfo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {

    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final double DECREASE_RATIO = 0.05;

    private final ProductRepository productRepository;

    @Transactional
    public Long createProduct(
            List<String> imageUrls,
            ProductCreateRequest request,
            User user
    ) {
        Product product = Product.of(
                request.name(),
                request.initialPrice(),
                request.content(),
                request.category(),
                request.deadline(),
                ProductState.ON_SALE,
                user.getId(),
                imageUrls
        );

        productRepository.save(product);
        productRepository.insertProductMeta(ProductMeta.builder()
                .productId(product.getId())
                .score(0)
                .bookmarkCount(0)
                .build()
        );

        return product.getId();
    }

    public ProductDetailResponse find(Long productId, User user) {
        return productRepository.findDetailById(productId)
                .orElseThrow(() -> new BadRequestException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    /**
     * 상품 전체 조회 API
     * DEFAULT_PAGE_SIZE(10) 개를 ProductMeta 통계 테이블에서 먼저 뽑아오고
     * In Query 로 필요한 데이터 긁어오는 방식으로 처리
     * ORDER - (Product.score DESC, Product.id DESC)
     * PageToken form : {score}@{productId}
     */
    public PageInfo<ProductResponse> findAll(
            String pageToken
    ) {
        List<ProductMeta> productMetas = productRepository.findProductMetasWithPaging(pageToken);
        Map<Long, Pair<Integer, Double>> productIdsToBookmarkCount = mapProductIdsToBookmarkCount(productMetas);
        List<Long> productIds = extractProductIdsFromMeta(productMetas);
        List<ProductWithBidAmountInfo> productInfos = productRepository.findWithBidAmountByIdsIn(productIds);
        var data = productInfos.stream()
                .map(productInfo -> {
                    var productIdToMeta = productIdsToBookmarkCount.get(productInfo.id());
                    return ProductResponse.builder()
                            .id(productInfo.id())
                            .name(productInfo.name())
                            .currentBid(productInfo.currentBid())
                            .deadline(productInfo.deadline())
                            .imageUrl(productInfo.imageUrl().isEmpty() ? "" : productInfo.imageUrl().get(0))
                            .buyerName(productInfo.sellerNickname())
                            .bookmarkCount(productIdToMeta.getLeft())
                            .score(productIdToMeta.getRight())
                            .build();
                    // score, id 순으로 이미 PAGE_SIZE 개를 DB 에서 가져와서 메모리에서 정렬해도 상관없음.
                }).sorted(
                        Comparator
                                .comparing(ProductResponse::score).reversed()
                                .thenComparing(ProductResponse::id, Comparator.reverseOrder())
                ).toList();
        return PageInfo.of(data, DEFAULT_PAGE_SIZE, ProductResponse::score, ProductResponse::id);
    }

    public void deleteProduct(Long productId, User user) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BadRequestException(ErrorCode.PRODUCT_NOT_FOUND));

        if (!product.isOwner(user)) {
            throw new BadRequestException(ErrorCode.HAS_NOT_PERMISSION_REMOVE_PRODUCT);
        }

        productRepository.delete(product);
    }

    /**
     * Batch Scheduling - Product 북마크 수, 지난 날짜를 종합적으로 평가하여 ProductMeta 테이블에 UPDATE
     * 만약 Product 의 개수가 많아진다면 PK로 구간을 나눠서 병렬 처리하는게 좋음.
     * 그럼에도 느릴 경우 (DB 의 CPU, Memory 한계로) Sharding 을 고려
     * shard 의 개수가 불변이라면 % (모듈러) 해싱으로 가능할 거 같다.
     * 당연히 위 방식은 샤드 개수가 바뀌면 취약함. 바뀐다면 그때가서 날잡고 고치거나 다른 방법을 생각하는게 맞는듯..?
     */
    public void calculateScores() {
        var productDateInfo = productRepository.findAllProductDate();
        var productIds = extractProductIds(productDateInfo);
        var bookmarkCounts = productRepository.getAllBookmarkCountsInIds(productIds);
        var productIdsToBookmarkCounts = mapProductIdsToBookmarkCounts(bookmarkCounts);
        var productStatisticInfos = productDateInfo.stream()
                .map(info -> {
                    long daysPassed = ChronoUnit.DAYS.between(info.createdAt().toLocalDate(), LocalDate.now());
                    int bookmarkCount = productIdsToBookmarkCounts.getOrDefault(info.id(), 0);
                    double score = bookmarkCount * Math.pow(1 - DECREASE_RATIO, daysPassed);
                    return ProductStatisticInfo.of(info.id(), bookmarkCount, score);
                })
                .toList();

        productRepository.updateScores(productStatisticInfos);
    }

    private Map<Long, Integer> mapProductIdsToBookmarkCounts(List<ProductBookmarkCount> bookmarkCounts) {
        return bookmarkCounts.stream()
                .collect(Collectors.toMap(
                        ProductBookmarkCount::id,
                        ProductBookmarkCount::count
                ));
    }

    private List<Long> extractProductIds(List<ProductDateInfo> productDateInfo) {
        return productDateInfo.stream()
                .map(ProductDateInfo::id)
                .toList();
    }

    private List<Long> extractProductIdsFromMeta(List<ProductMeta> productMetas) {
        return productMetas.stream()
                .map(ProductMeta::getProductId)
                .toList();
    }

    private Map<Long, Pair<Integer, Double>> mapProductIdsToBookmarkCount(List<ProductMeta> productMetas) {
        return productMetas.stream()
                .collect(Collectors.toMap(
                        ProductMeta::getProductId,
                        meta -> Pair.of(meta.getBookmarkCount(), meta.getScore())
                ));
    }
}