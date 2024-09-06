package com.palgona.palgona.product.event;

import org.springframework.web.multipart.MultipartFile;

public record ImageUploadEvent(MultipartFile file) {
}
