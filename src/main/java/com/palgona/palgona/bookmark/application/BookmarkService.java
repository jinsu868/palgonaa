package com.palgona.palgona.bookmark.application;

import com.palgona.palgona.common.dto.CustomMemberDetails;
import com.palgona.palgona.common.dto.response.SliceResponse;
import com.palgona.palgona.common.error.exception.BusinessException;
import com.palgona.palgona.bookmark.domain.Bookmark;
import com.palgona.palgona.member.domain.Member;
import com.palgona.palgona.product.domain.Product;
import com.palgona.palgona.bookmark.dto.response.BookmarkProductsResponse;
import com.palgona.palgona.bookmark.domain.BookmarkRepository;
import com.palgona.palgona.product.domain.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.palgona.palgona.common.error.code.BookmarkErrorCode.BOOKMARK_EXISTS;
import static com.palgona.palgona.common.error.code.BookmarkErrorCode.BOOKMARK_NOT_EXISTS;
import static com.palgona.palgona.common.error.code.ProductErrorCode.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class BookmarkService {
    private final BookmarkRepository bookmarkRepository;
    private final ProductRepository productRepository;


    public void createBookmark(Long productId, Member member) {
        Product product = findProduct(productId);

        validateBookmarkCreate(member, product);

        Bookmark bookmark = Bookmark.of(member, product);
        bookmarkRepository.save(bookmark);
    }

    public void deleteBookmark(Long productId, Member member){
        Product product = findProduct(productId);
        Bookmark bookmark = findBookmarkByProductAndMember(member, product);

        bookmarkRepository.delete(bookmark);
    }

    @Transactional(readOnly = true)
    public SliceResponse<BookmarkProductsResponse> readALlBookmark(
            CustomMemberDetails memberDetails,
            int cursor,
            int size
    ){
        Member member = memberDetails.getMember();
        PageRequest limit = PageRequest.of(cursor, size);

        //멤버의 상품찜 목록에 있는 상품 전체 가져오기
        // 상품 정보 + 입찰 최신 정보 + 해당 상품의 북마크 개수 + 상품의 첫번째 이미지
        // 이때 삭제된 상품은 제외한다.
        List<BookmarkProductsResponse> queryResults = bookmarkRepository.findBookmarkedProductsByMember(member, limit);

        return convertToSlice(queryResults, cursor);
    }


    private SliceResponse<BookmarkProductsResponse> convertToSlice(List<BookmarkProductsResponse> items, int nowCursor){
        boolean hasNext = true;
        if(items.isEmpty()) {
            hasNext = false;
        }

        String nextCursor = String.valueOf(nowCursor + 1);

        return SliceResponse.of(items, hasNext, nextCursor);
    }

    private void validateBookmarkCreate(Member member, Product product) {
        bookmarkRepository.findByMemberAndProduct(member, product)
                .ifPresent(b -> {throw new BusinessException(BOOKMARK_EXISTS);
                });
    }

    private Product findProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new BusinessException(NOT_FOUND));
    }

    private Bookmark findBookmarkByProductAndMember(Member member, Product product) {
        return bookmarkRepository.findByMemberAndProduct(member, product)
                .orElseThrow(() -> new BusinessException(BOOKMARK_NOT_EXISTS));
    }
}
