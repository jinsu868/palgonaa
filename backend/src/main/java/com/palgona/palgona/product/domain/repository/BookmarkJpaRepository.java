package com.palgona.palgona.product.domain.repository;

import com.palgona.palgona.product.domain.Bookmark;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookmarkJpaRepository extends JpaRepository<Bookmark, Long> {
    Optional<Bookmark> findByUserIdAndProductId(Long userId, Long productId);
}
