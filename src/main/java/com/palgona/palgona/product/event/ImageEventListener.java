package com.palgona.palgona.product.event;

import com.palgona.palgona.image.application.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ImageEventListener {

    private final S3Service s3Service;

    @EventListener
    @Async("s3AsyncExecutor")
    public void uploadImage(ImageUploadEvent imageUploadEvent) {
        s3Service.upload(imageUploadEvent.file());
    }
}
