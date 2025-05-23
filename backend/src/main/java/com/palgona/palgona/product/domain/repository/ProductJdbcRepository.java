package com.palgona.palgona.product.domain.repository;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.palgona.palgona.common.util.JsonUtils;
import com.palgona.palgona.product.domain.ProductCategory;
import com.palgona.palgona.common.util.PageTokenParser;
import com.palgona.palgona.product.domain.ProductMeta;
import com.palgona.palgona.product.dto.ProductStatisticInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.palgona.palgona.product.domain.ProductState;
import com.palgona.palgona.product.dto.response.ProductDetailResponse;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ProductJdbcRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final JdbcTemplate jdbcTemplate;
    private final JsonUtils jsonUtils;

    public Optional<ProductDetailResponse> findDetailProductInfo(Long productId) {
        String sql = """
        SELECT p.product_id, p.name, p.content, p.category, p.state, p.deadline, p.image_urls,
               b.amount, u.nickname
        FROM product p
        LEFT JOIN bid b ON b.product_id = p.product_id
        INNER JOIN user u ON u.user_id = p.user_id
        WHERE p.product_id = ?
        """;

        List<ProductDetailResponse> result = jdbcTemplate.query(sql,
                (rs, rowNum) -> {
                    String imageUrlsJson = rs.getString("image_urls");
                    List<String> imageUrls = jsonUtils.convertToObject(
                            imageUrlsJson,
                            new TypeReference<List<String>>() {}
                    );

                    return new ProductDetailResponse(
                            rs.getLong("product_id"),
                            rs.getString("name"),
                            rs.getString("content"),
                            ProductCategory.valueOf(rs.getString("category")),
                            ProductState.valueOf(rs.getString("state")),
                            rs.getTimestamp("deadline").toLocalDateTime(),
                            imageUrls,
                            rs.getObject("amount", Integer.class),
                            rs.getString("nickname")
                    );
                },
                productId
        );

        return result.stream().findFirst();
    }

    public void updateProductsToExpireInIds(List<Long> productIds) {
        if (productIds.isEmpty()) {
            return;
        }

        String sql = "UPDATE product SET state = 'EXPIRED' WHERE product_id IN (:productIds)";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("productIds", productIds);

        namedParameterJdbcTemplate.update(sql, params);
    }

    public void update(Long productId, List<String> imageUrls, ProductCategory category, String name, String content) {
        String images = jsonUtils.convertToJson(imageUrls);
        String sql = "UPDATE product SET image_urls = :imageUrls, category = :category, name = :name, "
                + "content = :content WHERE product_id = :productId";

        jdbcTemplate.update(sql, imageUrls, category, name, content, productId);
    }

    public void batchUpdate(List<ProductStatisticInfo> statisticInfos) {
        String sql = "UPDATE product_meta SET score = ?, bookmark_count = ? WHERE product_id = ?";
        jdbcTemplate.batchUpdate(
                sql,
                statisticInfos,
                statisticInfos.size(),
                (ps, info) -> {
                    ps.setDouble(1, info.score());
                    ps.setInt(2, info.bookmarkCount());
                    ps.setLong(3, info.productId());
                }
        );
    }

    public List<ProductMeta> findProductMetaInfoWithPaging(String pageToken, int pageSize) {
        RowMapper<ProductMeta> rowMapper = (rs, rowNum) ->
                new ProductMeta(rs.getLong("product_id"), rs.getInt("bookmark_count"), rs.getDouble("score"));

        if (pageToken == null) {
            String sql = """
                    SELECT pm.product_id,
                           pm.bookmark_count,
                           pm.score
                    FROM product_meta pm
                    ORDER BY pm.score DESC,
                    pm.product_id DESC
                    LIMIT ?
                    """;
            return jdbcTemplate.query(sql, rowMapper, pageSize);
        }

        Pair<String, String> token = PageTokenParser.parsePageToken(pageToken);
        String sql = """
            SELECT pm.product_id, pm.score, pm.bookmark_count
            FROM product_meta pm
            WHERE (score < ?) OR (score = ? AND product_id < ?)
            ORDER BY score DESC, product_id DESC
            LIMIT ?
        """;

        return jdbcTemplate.query(sql,
                rowMapper,
                Double.valueOf(token.getLeft()),
                Double.valueOf(token.getLeft()),
                Long.valueOf(token.getRight()),
                pageSize
        );
    }

    public void insert(ProductMeta productMeta) {
        String sql = "INSERT INTO product_meta (product_id, score, bookmark_count) " +
                "VALUES (:productId, :score, :bookmarkCount)";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("productId", productMeta.getProductId())
                .addValue("score", productMeta.getScore())
                .addValue("bookmarkCount", productMeta.getBookmarkCount());

        namedParameterJdbcTemplate.update(sql, params);
    }
}
