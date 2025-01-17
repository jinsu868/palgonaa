package com.palgona.palgona.fcm.domain;

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
public class SilentNotifications {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name ="product_id", nullable = false)
    Product product;

    @Builder
    SilentNotifications(Member member, Product product){
        this.member = member;
        this.product = product;
    }
}
