package com.palgona.palgona.purchase.domain.repository;

import com.palgona.palgona.purchase.domain.Purchase;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PurchaseJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    public void bulkInsert(List<Purchase> purchases) {
        String sql = "INSERT INTO purchase(product_id, user_id, amount, state, created_at) VALUES(?, ?, ?, ?, ?)";


        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Purchase purchase = purchases.get(i);
                ps.setLong(1, purchase.getProductId());
                ps.setLong(2, purchase.getUserId());
                ps.setInt(3, purchase.getAmount());
                ps.setString(4, purchase.getState().toString());
                ps.setTimestamp(5, Timestamp.valueOf(purchase.getCreatedAt()));
            }

            @Override
            public int getBatchSize() {
                return purchases.size();
            }
        });
    }
}
