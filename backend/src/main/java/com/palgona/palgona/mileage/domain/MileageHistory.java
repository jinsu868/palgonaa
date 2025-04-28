package com.palgona.palgona.mileage.domain;

import com.palgona.palgona.common.entity.BaseTimeEntity;
import com.palgona.palgona.member.domain.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class MileageHistory extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer beforeMileage;

    @Column(nullable = false)
    private Integer afterMileage;

    @Column(nullable = false)
    private Integer amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MileageState state;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    public static MileageHistory of(
            int beforeMileage,
            int afterMileage,
            int amount,
            MileageState state,
            Member member
    ) {
        return new MileageHistory(
                beforeMileage,
                afterMileage,
                amount,
                state,
                member
        );
    }

    private MileageHistory(
            int beforeMileage,
            int afterMileage,
            int amount,
            MileageState state,
            Member member
    ){
        this.beforeMileage = beforeMileage;
        this.afterMileage = afterMileage;
        this.amount = amount;
        this.state = state;
        this.member = member;
    }
}