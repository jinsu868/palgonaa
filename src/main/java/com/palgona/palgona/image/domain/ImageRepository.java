package com.palgona.palgona.image.domain;

import com.palgona.palgona.image.domain.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ImageRepository extends JpaRepository<Image, Long> {
    @Query("""
        select i
        from Image i
        where i.imageUrl in :urls
    """)
    List<Image> findImageByImageUrls(List<String> urls);

    @Modifying
    @Query("""
     delete from Image i
     where i.imageUrl in :urls
    """)
    void deleteByImageUrls(List<String> urls);

    @Query("select i from Image i join ProductImage pi on pi.image = i where pi.product.id = :productId")
    List<Image> findAllByProductId(Long productId);
}