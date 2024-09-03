package com.palgona.palgona.product.domain;

import com.palgona.palgona.image.domain.Image;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "product_image", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"product_id", "image_id"})
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ProductImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id", nullable = false)
    private Image image;

    @Builder
    public ProductImage(Product product, Image image){
        this.product = product;
        this.image = image;
    }
}