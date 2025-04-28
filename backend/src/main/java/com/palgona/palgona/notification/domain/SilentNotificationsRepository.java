package com.palgona.palgona.notification.domain;

import com.palgona.palgona.fcm.domain.SilentNotifications;
import com.palgona.palgona.member.domain.Member;
import com.palgona.palgona.product.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SilentNotificationsRepository extends JpaRepository<SilentNotifications, Long> {
    Optional<SilentNotifications> findByMemberAndProduct(Member member, Product product);
}
