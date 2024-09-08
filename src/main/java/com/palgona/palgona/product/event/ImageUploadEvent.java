package com.palgona.palgona.product.event;

import com.palgona.palgona.image.dto.ImageUploadRequest;
import java.util.List;

public record ImageUploadEvent(
        List<ImageUploadRequest> imageUploadRequests
) {

    public static ImageUploadEvent from(List<ImageUploadRequest> imageUploadRequests) {
        return new ImageUploadEvent(imageUploadRequests);
    }
}
