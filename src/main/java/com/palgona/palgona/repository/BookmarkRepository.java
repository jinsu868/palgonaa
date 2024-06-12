package com.palgona.palgona.repository;

import com.palgona.palgona.domain.bookmark.Bookmark;
import com.palgona.palgona.domain.member.Member;
import com.palgona.palgona.domain.product.Product;
import com.palgona.palgona.dto.BookmarkProductsResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    Optional<Bookmark> findByMemberAndProduct(Member member, Product product);

    @Modifying
    @Query("""
        delete from Bookmark b
        where b.product = :product
    """)
    void deleteByProduct(Product product);

    @Query("""
        select new com.palgona.palgona.dto.BookmarkProductsResponse(
            p.id,
            p.name,
            p.deadline,
            p.createdAt,
            (select b.price from Bidding b where b.product = p order by b.createdAt DESC limit 1),
            (select count(bm) from Bookmark bm where bm.product = p),
            (select i.imageUrl from ProductImage pi join Image i on pi.image = i where pi.product = p order by pi.id ASC limit 1)
        )
        from Bookmark b
        join b.product p
        where b.member = :member and p.productState != 'DELETED'
        group by p.id, p.name, p.deadline, p.createdAt
    """)
    List<BookmarkProductsResponse> findBookmarkedProductsByMember(Member member, Pageable pageable);
}
