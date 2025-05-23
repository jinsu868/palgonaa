package com.palgona.palgona.bidding.domain.repository;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.palgona.palgona.bidding.domain.BidEvent;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class BidJdbcRepository {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedJdbcTemplate;

    public void insert(BidEvent bidEvent) {
        String sql = "INSERT INTO bid_event (product_id, user_id, amount, created_at) VALUES (?, ?, ?, ?)";

        jdbcTemplate.update(
                sql,
                bidEvent.getProductId(),
                bidEvent.getUserId(),
                bidEvent.getAmount(),
                Timestamp.valueOf(bidEvent.getCreatedAt())
        );
    }

    public List<BidEvent> selectMaxBidEventGroupByProductId(List<Long> productIds) {
        if (productIds.isEmpty()) {
            return Collections.emptyList();
        }

        String sql = """
            SELECT be.id,
                   be.product_id,
                   be.user_id,
                   be.amount,
                   be.created_at
              FROM bid_event be
              INNER JOIN (
                   SELECT product_id,
                          MAX(amount) AS max_amount
                     FROM bid_event
                    WHERE product_id IN (:productIds)
                    GROUP BY product_id
              ) sub
               ON be.product_id = sub.product_id
               AND be.amount = sub.max_amount
               WHERE NOT EXISTS (
               SELECT 1
               FROM bid_event be2
               WHERE be2.product_id = be.product_id
               AND be2.amount = be.amount
               AND be2.created_at < be.created_at
            )
        """;

        var params = new MapSqlParameterSource()
                .addValue("productIds", productIds);

        return namedJdbcTemplate.query(sql, params, (rs, rowNum) -> BidEvent.builder()
                .id(rs.getLong("id"))
                .productId(rs.getLong("product_id"))
                .userId(rs.getLong("user_id"))
                .amount(rs.getInt("amount"))
                .build());
    }

    public int[] bulkUpdateBids(List<BidEvent> bidEvents) {
        if (bidEvents.isEmpty()) {
            return new int[0];
        }

        String sql = "UPDATE bid SET user_id = :userId, amount = :amount, created_at = :created_at WHERE product_id = :productId";
        MapSqlParameterSource[] batchParams = bidEvents.stream()
                .map(e -> new MapSqlParameterSource()
                        .addValue("userId",    e.getUserId())
                        .addValue("amount",    e.getAmount())
                        .addValue("productId", e.getProductId())
                        .addValue("created_at", Timestamp.valueOf(e.getCreatedAt()))
                )
                .toArray(MapSqlParameterSource[]::new);

        return namedJdbcTemplate.batchUpdate(sql, batchParams);
    }
}
