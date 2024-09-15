package com.palgona.palgona.product.domain;

import static com.palgona.palgona.common.error.code.ProductErrorCode.INVALID_CATEGORY;
import static com.palgona.palgona.common.error.code.ProductErrorCode.INVALID_DEADLINE;
import static com.palgona.palgona.common.error.code.ProductErrorCode.INVALID_PRICE;

import com.palgona.palgona.common.entity.BaseTimeEntity;
import com.palgona.palgona.common.error.exception.BusinessException;
import com.palgona.palgona.member.domain.Member;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Product extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer currentPrice;

    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Column(nullable = false)
    private LocalDateTime deadline;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ProductState productState;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> productImages = new ArrayList<>();

    public static Product of(
            String name,
            Integer currentPrice,
            String content,
            String category,
            LocalDateTime deadline,
            ProductState productState,
            Member member
    ) {
        validateInitialPrice(currentPrice);
        validateCategory(category);
        validateDeadline(deadline);

        return new Product(
                name,
                currentPrice,
                content,
                Category.valueOf(category),
                deadline,
                productState,
                member
        );
    }

    private static void validateInitialPrice(Integer currentPrice) {
        if (currentPrice < 0) {
            throw new BusinessException(INVALID_PRICE);
        }
    }

    private static void validateCategory(String category) {
        try {
            Category.valueOf(category);
        } catch (Exception e) {
            throw new BusinessException(INVALID_CATEGORY);
        }
    }

    private static void validateDeadline(LocalDateTime deadline) {
        if(deadline.isBefore(LocalDateTime.now().plusDays(1))){
            throw new BusinessException(INVALID_DEADLINE);
        }
    }

    private Product(
            String name,
            Integer currentPrice,
            String content,
            Category category,
            LocalDateTime deadline,
            ProductState productState,
            Member member) {
        this.name = name;
        this.currentPrice = currentPrice;
        this.content = content;
        this.category = category;
        this.deadline = deadline;
        this.productState = productState;
        this.member = member;
    }

    public void updateProductState(ProductState productState) {this.productState = productState;}

    public boolean isDeadlineReached() {
        return LocalDateTime.now().isAfter(this.deadline);
    }

    public boolean isOwner(Member member){
        return this.member.getId() == member.getId();
    }

    public void addProductImages(List<ProductImage> productImages) {
        for (ProductImage productImage : productImages) {
            addProductImage(productImage);
        }
    }

    public void addProductImage(ProductImage productImage) {
        productImages.add(productImage);
    }

    public void expire() {
        productState = ProductState.EXPIRED;
    }

    public void sell() {
        productState = ProductState.SOLD_OUT;
    }

    public void updatePrice(int price) {
        currentPrice = price;
    }

}
