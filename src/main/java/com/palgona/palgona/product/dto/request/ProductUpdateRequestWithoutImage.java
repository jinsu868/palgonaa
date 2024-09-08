package com.palgona.palgona.product.dto.request;

import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

public record ProductUpdateRequestWithoutImage(
        String name,
        Integer initialPrice,
        String content,
        String category,
        List<String> deletedImageUrls,
        LocalDateTime deadline
) {
}
