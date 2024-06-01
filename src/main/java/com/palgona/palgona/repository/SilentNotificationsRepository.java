package com.palgona.palgona.repository;

import com.palgona.palgona.domain.SilentNotifications;
import com.palgona.palgona.domain.member.Member;
import com.palgona.palgona.domain.product.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SilentNotificationsRepository extends JpaRepository<SilentNotifications, Long> {
    Optional<SilentNotifications> findByMemberAndProduct(Member member, Product product);
}
