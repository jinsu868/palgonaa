package com.palgona.palgona.repository.product;

import com.palgona.palgona.domain.member.Member;
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


    @Query(""" 
           select new com.palgona.palgona.repository.product.querydto.ProductDetailQueryResponse(
           p.id,
           p.name,
           p.content,
           p.category,
           p.productState,
           p.deadline,
           p.createdAt,
           m.id,
           m.nickName,
           m.profileImage,
           coalesce((select bd.price from Bidding bd where bd.product = p order by bd.createdAt desc limit 1), p.initialPrice),
           (select count(bm) from Bookmark bm where bm.product = p),
           (select count(cr) from ChatRoom cr where cr.product = p),
           case when (select count(s) from SilentNotifications s where s.member = :member and s.product = p) > 0 then true else false end
           )
           from Product p
           join p.member m
           where p.id = :productId
            
""")
    Optional<ProductDetailQueryResponse> findProductDetailsById(Long productId, Member member);


}
