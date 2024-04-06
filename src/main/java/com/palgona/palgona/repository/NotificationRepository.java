package com.palgona.palgona.repository;

import com.palgona.palgona.domain.notification.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    @Query("SELECT n FROM Notification n WHERE n.member.id = :memberId AND n.id < :cursorId ORDER BY n.id DESC")
    List<Notification> findByMemberIdAndCursor(Long memberId, Long cursorId, Pageable pageable);
}
