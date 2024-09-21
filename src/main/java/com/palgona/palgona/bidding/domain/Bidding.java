package com.palgona.palgona.bidding.domain;

import com.palgona.palgona.common.entity.BaseTimeEntity;
import com.palgona.palgona.member.domain.Member;
import com.palgona.palgona.product.domain.Product;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Bidding extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "price")
    private int price;

    @Enumerated(EnumType.STRING)
    @Column(name = "state")
    private BiddingState state;

    public static Bidding of(
            Product product,
            Member member,
            int price
    ) {
        return new Bidding(
                product,
                member,
                price
        );
    }

    private Bidding(Product product, Member member, int price) {
        this.product = product;
        this.member = member;
        this.price = price;
        this.state = BiddingState.ATTEMPT;
    }

    public void updateState(BiddingState state) {
        this.state = state;
    }

    public void success() {
        state = BiddingState.SUCCESS;
    }

    public void fail() {
        state = BiddingState.FAILED;
    }

    public void cancel() {
        state = BiddingState.CANCEL;
    }

    public boolean isOwner(Member member) {
        return this.member.getId() == member.getId();
    }
}
