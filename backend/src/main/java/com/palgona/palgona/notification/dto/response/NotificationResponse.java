package com.palgona.palgona.notification.dto.response;

import com.palgona.palgona.notification.domain.Notification;
import com.palgona.palgona.notification.domain.Type;

public record NotificationResponse(
        Long id,
        String title,
        String body,
        Type type,
        Long targetId){

    public static NotificationResponse from(Notification notification){
        return new NotificationResponse(
                notification.getId(),
                notification.getTitle(),
                notification.getBody(),
                notification.getType(),
                notification.getTargetId()
        );
    }
}
