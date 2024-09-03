package com.palgona.palgona.product.presentation;

import com.palgona.palgona.common.dto.CustomMemberDetails;
import com.palgona.palgona.common.dto.response.SliceResponse;
import com.palgona.palgona.product.domain.Category;
import com.palgona.palgona.product.domain.SortType;
import com.palgona.palgona.product.dto.request.ProductCreateRequest;
import com.palgona.palgona.product.dto.response.ProductDetailResponse;
import com.palgona.palgona.product.dto.request.ProductUpdateRequest;
import com.palgona.palgona.product.dto.response.ProductPageResponse;
import com.palgona.palgona.product.application.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "상품 등록 api", description = "상품 정보와 상품 사진들을 받아서 상품 등록을 진행한다.")
    public ResponseEntity<Void> createProduct(
            @ModelAttribute ProductCreateRequest request,
            @AuthenticationPrincipal CustomMemberDetails member
    ){

        productService.createProduct(request, request.files(), member);

        return ResponseEntity.ok()
                .build();
    }

    @GetMapping("/{productId}")
    @Operation(summary = "상품 상세 조회 api", description = "상품 id를 받아 상품 상세 정보를 보여준다.")
    public ResponseEntity<ProductDetailResponse> readProduct(@PathVariable Long productId, @AuthenticationPrincipal CustomMemberDetails member){
        ProductDetailResponse productDetailResponse = productService.readProduct(productId, member);

        return ResponseEntity.ok()
                .body(productDetailResponse);
    }

    @GetMapping
    public ResponseEntity<SliceResponse<ProductPageResponse>> readProducts(
            @RequestParam(defaultValue = "LATEST") SortType sortType,
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) String searchWord,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false, defaultValue = "20") int pageSize
    ) {

        SliceResponse<ProductPageResponse> response = productService.readProducts(
                sortType, category, searchWord, cursor, pageSize);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{productId}")
    @Operation(summary = "상품 삭제 api", description = "상품 id를 받아 해당 상품 삭제 처리를 진행한다. ")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long productId, @AuthenticationPrincipal CustomMemberDetails member){

        productService.deleteProduct(productId, member);

        return ResponseEntity.ok().build();
    }

    @PutMapping(
            value = "/{productId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "상품 수정 api", description = "상품 id를 받아 해당 상품 수정 처리를 진행한다.")
    public ResponseEntity<Void> updateProduct(
            @PathVariable Long productId,
            @ModelAttribute ProductUpdateRequest request,
            @AuthenticationPrincipal CustomMemberDetails member
    ){

        productService.updateProduct(productId, request, request.files(), member);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{productId}/Notifications")
    @Operation(summary = "상품 알림 무시 api", description = "상품 id를 받아 해당 상품에 대한 알림 무시를 활성화한다.")
    public ResponseEntity<Void> turnOffProductNotification(@PathVariable Long productId, @AuthenticationPrincipal CustomMemberDetails member){

        productService.turnOffProductNotification(productId, member);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{productId}/Notifications")
    @Operation(summary = "상품 알림 활성화 api", description = "상품 id를 받아 해당 상품에 대한 알림을 다시 활성화한다.")
    public ResponseEntity<Void> turnOnProductNotification(@PathVariable Long productId, @AuthenticationPrincipal CustomMemberDetails member){

        productService.turnOnProductNotification(productId, member);

        return ResponseEntity.ok().build();
    }

}