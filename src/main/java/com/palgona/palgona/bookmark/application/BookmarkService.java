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
@Transactional
public class BookmarkService {
    private final BookmarkRepository bookmarkRepository;
    private final ProductRepository productRepository;

    public void createBookmark(Long productId, CustomMemberDetails memberDetails){
        Member member = memberDetails.getMember();

        //1. 해당 상품이 존재하는지 확인
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(NOT_FOUND));

        //2. 이미 추가된 찜인지 확인
        bookmarkRepository.findByMemberAndProduct(member, product)
                .ifPresent(b -> {
                    throw new BusinessException(BOOKMARK_EXISTS);
                });

        //3. 찜 추가
        Bookmark bookmark = Bookmark.builder()
                .member(member)
                .product(product)
                .build();

        bookmarkRepository.save(bookmark);
    }

    public void deleteBookmark(Long productId, CustomMemberDetails memberDetails){
        Member member = memberDetails.getMember();

        //1. 해당 상품이 존재하는지 확인
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(NOT_FOUND));

        //2. 해당 찜이 존재하는지 확인
        Bookmark bookmark = bookmarkRepository.findByMemberAndProduct(member, product)
                .orElseThrow(() -> new BusinessException(BOOKMARK_NOT_EXISTS));

        //3. 찜 삭제
        bookmarkRepository.delete(bookmark);
    }

    @Transactional(readOnly = true)
    public SliceResponse<BookmarkProductsResponse> readALlBookmark(CustomMemberDetails memberDetails, int cursor, int size){
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
}
