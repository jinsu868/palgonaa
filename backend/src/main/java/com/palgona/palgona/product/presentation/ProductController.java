package com.palgona.palgona.product.presentation;

import com.palgona.palgona.product.application.BookmarkService;
import com.palgona.palgona.product.dto.request.ProductUpdateRequest;
import com.palgona.palgona.product.dto.response.ProductResponse;
import java.net.URI;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.palgona.palgona.common.model.PageInfo;
import com.palgona.palgona.auth.annotation.AuthUser;
import com.palgona.palgona.image.application.S3Service;
import com.palgona.palgona.product.dto.request.ProductCreateRequest;
import com.palgona.palgona.product.dto.response.ProductDetailResponse;
import com.palgona.palgona.product.application.ProductService;
import com.palgona.palgona.user.domain.User;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v2/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final S3Service s3Service;
    private final BookmarkService bookmarkService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> createProduct(
            @RequestPart List<MultipartFile> files,
            @RequestPart ProductCreateRequest request,
            @AuthUser User user
    ){
        List<String> imageUrls = s3Service.uploadFiles(files);
        Long productId = productService.createProduct(imageUrls, request, user);

        return ResponseEntity.created(URI.create("/api/v1/products/" + productId)).build();
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductDetailResponse> find(
            @PathVariable Long productId,
            @AuthUser User user
    ){
        return ResponseEntity.ok().body(productService.find(productId, user));
    }

    @GetMapping
    public ResponseEntity<PageInfo<ProductResponse>> findAll(
            @AuthUser User user,
            @RequestParam(required = false) String pageToken
    ) {
        return ResponseEntity.ok(productService.findAll(pageToken));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable Long productId,
            @AuthUser User user
    ) {
        productService.deleteProduct(productId, user);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{productId}/bookmark")
    public ResponseEntity<Void> bookmark(
            @PathVariable Long productId,
            @AuthUser User user
    ) {
        bookmarkService.bookmarkToggle(productId, user);
        return ResponseEntity.noContent().build();
    }
}