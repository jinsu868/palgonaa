package com.palgona.palgona.product.domain.repository;

import com.palgona.palgona.product.domain.Product;

import com.palgona.palgona.product.domain.ProductCategory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;


public interface ProductJpaRepository extends JpaRepository<Product, Long>, ProductQueryRepository {
    @Modifying
    @Query("update Product p set p.deleted = true where p.id = :id")
    @Transactional
    public void deleteById(Long id);

    @Query("SELECT p FROM Product p WHERE p.state = 'ON_SALE'")
    List<Product> findAllOngoing();
}
