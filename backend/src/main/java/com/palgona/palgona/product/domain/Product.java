package com.palgona.palgona.product.domain;

import static com.palgona.palgona.common.error.ErrorCode.INVALID_PRICE;
import static com.palgona.palgona.common.error.ErrorCode.INVALID_PRODUCT_DEADLINE;

import com.vladmihalcea.hibernate.type.json.JsonType;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import com.palgona.palgona.common.entity.BaseTimeEntity;
import com.palgona.palgona.common.error.BadRequestException;
import com.palgona.palgona.user.domain.User;

import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.Type;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("deleted = false")
public class Product extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "content", nullable = true)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private ProductCategory category;

    @Column(name = "deadline", nullable = false)
    private LocalDateTime deadline;

    @Type(JsonType.class)
    @Column(columnDefinition = "json", name = "image_urls")
    private List<String> imageUrls;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    private ProductState state;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted;

    public static Product of(
            String name,
            Integer currentPrice,
            String content,
            ProductCategory category,
            LocalDateTime deadline,
            ProductState productState,
            Long userId,
            List<String> imageUrls
    ) {
        validateInitialPrice(currentPrice);
        validateDeadline(deadline);

        return new Product(
                name,
                content,
                category,
                deadline,
                productState,
                userId,
                imageUrls
        );
    }

    private static void validateInitialPrice(Integer currentPrice) {
        if (currentPrice < 0) {
            throw new BadRequestException(INVALID_PRICE);
        }
    }

    private static void validateDeadline(LocalDateTime deadline) {
        if(deadline.isBefore(LocalDateTime.now().plusDays(1))){
            throw new BadRequestException(INVALID_PRODUCT_DEADLINE);
        }
    }

    private Product(
            String name,
            String content,
            ProductCategory productCategory,
            LocalDateTime deadline,
            ProductState productState,
            Long userId,
            List<String> imageUrls
    ) {
        this.name = name;
        this.content = content;
        this.category = productCategory;
        this.deadline = deadline;
        this.state = productState;
        this.userId = userId;
        this.imageUrls = imageUrls;
        this.deleted = false;
    }

    public void updateProductState(ProductState productState) {
        this.state = productState;
    }

    public boolean isDeadlineReached() {
        return LocalDateTime.now().isAfter(this.deadline);
    }

    public boolean isOwner(User user){
        return userId.equals(user.getId());
    }

    public void expire() {
        state = ProductState.EXPIRED;
    }

    public void sell() {
        state = ProductState.SOLD_OUT;
    }

    public boolean isOnSale() {
        return state == ProductState.ON_SALE;
    }
}
