package com.palgona.palgona.product.domain;

import com.palgona.palgona.image.domain.Image;
import com.palgona.palgona.product.domain.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    @Query("""
        select i.imageUrl
        from ProductImage pi
        left join pi.image i
        where pi.product.id = :productId
    """)
    List<String> findProductImageUrlsByProduct(Long productId);

    @Modifying
    @Query("""
        delete from ProductImage pi
        where pi.image in :images
    """)
    void deleteByImageIds(List<Image> images);
}
