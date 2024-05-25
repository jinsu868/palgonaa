package com.palgona.palgona.repository.product;

import com.palgona.palgona.domain.product.Product;
import com.palgona.palgona.repository.product.querydto.ProductDetailQueryResponse;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>, ProductRepositoryCustom {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p from Product p where p.id = :id")
    Optional<Product> findByIdWithPessimisticLock(Long id);
}
