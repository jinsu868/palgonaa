package com.palgona.palgona.bookmark.presentation;

import com.palgona.palgona.common.dto.CustomMemberDetails;
import com.palgona.palgona.common.dto.response.SliceResponse;
import com.palgona.palgona.bookmark.dto.response.BookmarkProductsResponse;
import com.palgona.palgona.bookmark.application.BookmarkService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/bookmarks")
public class BookmarkController {
    private final BookmarkService bookmarkService;

    @PostMapping("/{productId}")
    @Operation(summary = "북마크 추가 api", description = "상품id를 받아서 북마크 추가를 진행한다.")
    public ResponseEntity<Void> createBookmark(@PathVariable Long productId, @AuthenticationPrincipal CustomMemberDetails member){

        bookmarkService.createBookmark(productId, member);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{productId}")
    @Operation(summary = "북마크 삭제 api", description = "상품id를 받아서 북마크 삭제를 진행한다.")
    public ResponseEntity<Void> deleteBookmark(@PathVariable Long productId, @AuthenticationPrincipal CustomMemberDetails member){

        bookmarkService.deleteBookmark(productId, member);

        return ResponseEntity.ok().build();
    }

    @GetMapping
    @Operation(summary = "북마크 리스트 조회 api", description = "해당 멤버의 상품찜 리스트를 보여준다.")
    public ResponseEntity<SliceResponse<BookmarkProductsResponse>> readALlBookmark(
            @AuthenticationPrincipal CustomMemberDetails memberDetails,
            @RequestParam(required = false, defaultValue = "0") int cursor,
            @RequestParam(defaultValue = "20") int size){

        SliceResponse<BookmarkProductsResponse> responses = bookmarkService.readALlBookmark(memberDetails, cursor, size);

        return ResponseEntity.ok(responses);
    }
}
