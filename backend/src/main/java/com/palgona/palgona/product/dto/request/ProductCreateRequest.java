package com.palgona.palgona.product.dto.request;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public record ProductCreateRequest(
        String name,
        Integer initialPrice,
        String content,
        String category,
        LocalDateTime deadline,
        List<MultipartFile> files
) {
    public static ProductCreateRequest of(
            ProductCreateRequestWithoutImage request,
            List<MultipartFile> files
    ) {
        return new ProductCreateRequest(
                request.name(),
                request.initialPrice(),
                request.content(),
                request.category(),
                request.deadline(),
                files
        );
    }
}
