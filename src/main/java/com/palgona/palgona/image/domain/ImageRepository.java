package com.palgona.palgona.image.domain;

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


    // TODO: 테스트 수정하고 삭제
    @Modifying
    @Query("""
     delete from Image i
     where i.imageUrl in :urls
    """)
    void deleteByImageUrls(List<String> urls);

    @Query("select i from Image i join ProductImage pi on pi.image = i where pi.product.id = :productId")
    List<Image> findAllByProductId(Long productId);

    @Modifying
    @Query("""
        delete from Image i
        where i.imageId in :imageIds
    """)
    void deleteByIds(List<Long> imageIds);

}