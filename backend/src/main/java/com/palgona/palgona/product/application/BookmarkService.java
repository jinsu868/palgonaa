package com.palgona.palgona.product.application;

import org.springframework.stereotype.Service;

import com.palgona.palgona.common.error.BadRequestException;
import com.palgona.palgona.common.error.ErrorCode;
import com.palgona.palgona.product.domain.Bookmark;
import com.palgona.palgona.product.domain.repository.BookmarkRepository;
import com.palgona.palgona.product.domain.repository.ProductRepository;
import com.palgona.palgona.user.domain.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final ProductRepository productRepository;

    public void bookmarkToggle(Long productId, User user) {
        if (!productRepository.existsById(productId)) {
            throw new BadRequestException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        var findBookmark = bookmarkRepository.findByUserIdAndProductId(user.getId(), productId);

        if (findBookmark.isEmpty()) {
            bookmarkRepository.save(
                    Bookmark.builder()
                            .userId(user.getId())
                            .productId(productId)
                            .build()
            );
            return;
        }

        bookmarkRepository.delete(findBookmark.get());
    }
}
