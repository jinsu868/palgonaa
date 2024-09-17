package com.palgona.palgona.product.domain;

import com.palgona.palgona.member.domain.Member;
import com.palgona.palgona.product.infrastructure.ProductRepositoryCustom;
import com.palgona.palgona.product.infrastructure.querydto.ProductDetailQueryResponse;
import com.palgona.palgona.product.infrastructure.querydto.ProductQueryResponse;
import jakarta.persistence.ColumnResult;
import jakarta.persistence.ConstructorResult;
import jakarta.persistence.LockModeType;
import jakarta.persistence.SqlResultSetMapping;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long>, ProductRepositoryCustom {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p from Product p where p.id = :id")
    Optional<Product> findByIdWithPessimisticLock(Long id);

    @Query("""
       SELECT p
       FROM Product p
       WHERE p.deadline < CURRENT_TIMESTAMP
       AND p.productState = 'ON_SALE'
        """)
    List<Product> findAuctionEndedProductsInOnSaleState();

    @Query("""
      SELECT p.id
      FROM Product p
      WHERE p.deadline < CURRENT_TIMESTAMP 
      AND p.productState = 'ON_SALE'
    """)
    List<Long> findAuctionEndedProductIdsInOnSaleState();
}
