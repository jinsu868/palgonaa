package com.palgona.palgona.repository;

import com.palgona.palgona.domain.member.Member;
import com.palgona.palgona.domain.notification.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("select n from Notification n where n.member = :member ORDER BY n.createdAt DESC")
    List<Notification> findAllByMember(Member member, Pageable pageable);

    @Query("select n from Notification n join fetch n.member where n.id = :id")
    Optional<Notification> findByIdWithMember(Long id);
}

