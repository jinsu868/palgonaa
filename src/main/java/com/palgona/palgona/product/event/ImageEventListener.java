package com.palgona.palgona.product.event;

import com.palgona.palgona.image.application.S3Service;
import com.palgona.palgona.image.domain.S3Client;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ImageEventListener {

    private final S3Service s3Service;

    @EventListener
    @Async("s3AsyncExecutor")
    @Retryable(
            backoff = @Backoff(delay = 400, multiplier = 2.0))
    public void uploadImages(ImageUploadEvent imageUploadEvent) {
        imageUploadEvent.imageUploadRequests().stream()
                .forEach(uploadRequest -> s3Service.upload(
                        uploadRequest.file(),
                        uploadRequest.uploadFileName()
                        )
                );
    }
}
