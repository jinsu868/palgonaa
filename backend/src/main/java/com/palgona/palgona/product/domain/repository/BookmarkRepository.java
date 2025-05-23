package com.palgona.palgona.product.domain.repository;

import com.palgona.palgona.product.domain.Bookmark;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BookmarkRepository {

    private final BookmarkJpaRepository bookmarkJpaRepository;

    public Bookmark save(Bookmark bookmark) {
        return bookmarkJpaRepository.save(bookmark);
    }

    public Optional<Bookmark> findByUserIdAndProductId(Long userId, Long productId) {
        return bookmarkJpaRepository.findByUserIdAndProductId(userId, productId);
    }

    public void delete(Bookmark bookmark) {
        bookmarkJpaRepository.delete(bookmark);
    }
}
