package com.palgona.palgona.image.application;

import com.palgona.palgona.image.domain.S3Client;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;

    @Value("${s3.base.url}")
    private String baseUrl;

    public String generateS3FileUrl(String fileName) {
        return baseUrl + fileName;
    }

    public String upload(MultipartFile file, String fileName) {
        return s3Client.upload(file, fileName);
    }

    public void deleteFile(String imageUrl) {
        s3Client.deleteFile(imageUrl);
    }

    public String uploadBase64Image(String base64Image) {
        return s3Client.uploadBase64Image(base64Image);
    }
}
