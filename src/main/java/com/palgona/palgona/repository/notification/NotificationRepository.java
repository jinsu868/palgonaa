package com.palgona.palgona.repository.notification;

import com.palgona.palgona.domain.notification.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long>, NotificationRepositoryCustom {
}

