package com.palgona.palgona.bookmark.domain;

import com.palgona.palgona.member.domain.Member;
import com.palgona.palgona.product.domain.Product;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Bookmark {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name ="product_id", nullable = false)
    Product product;

    public Bookmark(Member member, Product product){
        this.member = member;
        this.product = product;
    }

    public static Bookmark of(
            Member member,
            Product product
    ) {
        return new Bookmark(
                member,
                product
        );
    }
}
