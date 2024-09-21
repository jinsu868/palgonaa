package com.palgona.palgona.mileage.domain;

import com.palgona.palgona.member.domain.Member;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface MileageRepository extends JpaRepository<Mileage, Long> {
    Optional<Mileage> findByMember(Member member);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT m
        FROM Mileage m
        WHERE m.member = :member
    """)
    Optional<Mileage> findByMemberWithPessimisticLock(Member member);

    @Query("""
        SELECT m.balance
        FROM Mileage m
        WHERE m.member = :member
    """)
    Integer findBalanceByMember(Member member);

}
