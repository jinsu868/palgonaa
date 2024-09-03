package com.palgona.palgona.purchase.domain;

import static com.palgona.palgona.common.error.code.PurchaseErrorCode.INSUFFICIENT_PERMISSION;
import static com.palgona.palgona.common.error.code.PurchaseErrorCode.PURCHASE_EXPIRED;

import com.palgona.palgona.common.entity.BaseTimeEntity;
import com.palgona.palgona.common.error.exception.BusinessException;
import com.palgona.palgona.bidding.domain.Bidding;
import com.palgona.palgona.member.domain.Member;
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
import jakarta.persistence.OneToOne;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Purchase extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int purchasePrice;

    private String reason;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PurchaseState state;

    @Column(nullable = false)
    private LocalDateTime deadline;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bidding_id")
    private Bidding bidding;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Builder
    public Purchase(int purchasePrice, Bidding bidding, Member member) {
        this.purchasePrice = purchasePrice;
        this.bidding = bidding;
        this.member = member;
        this.reason = null;
        this.state = PurchaseState.WAIT;
        this.deadline = LocalDateTime.now().plusDays(1);
    }

    public void confirm() {
        state = PurchaseState.CONFIRM;
    }

    public void updateReason(String reason) {
        this.reason = reason;
    }

    public void cancel() {
        state = PurchaseState.CANCEL;
    }

    public void validateDeadline(LocalDateTime now) {
        if (deadline.isBefore(now)) {
            throw new BusinessException(PURCHASE_EXPIRED);
        }
    }

    public void validateOwner(Member member) {
        if (!this.member.equals(member)) {
            throw new BusinessException(INSUFFICIENT_PERMISSION);
        }
    }
}
