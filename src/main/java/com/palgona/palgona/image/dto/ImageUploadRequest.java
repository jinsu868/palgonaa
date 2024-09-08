package com.palgona.palgona.image.dto;

import org.springframework.web.multipart.MultipartFile;

public record ImageUploadRequest(
        MultipartFile file,
        String uploadFileName
) {
}
