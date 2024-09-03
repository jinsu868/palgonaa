package com.palgona.palgona.member.domain;

import com.palgona.palgona.member.infrastructure.MemberRepositoryCustom;
import jakarta.persistence.LockModeType;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {

    boolean existsByNickName(String nickName);

    Optional<Member> findBySocialId(String socialId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Member m where m.id = :id")
    Optional<Member> findByIdWithPessimisticLock(Long id);
}
