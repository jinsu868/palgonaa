package com.palgona.palgona.dto;

import com.palgona.palgona.domain.notification.Notification;
import com.palgona.palgona.domain.notification.Type;

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
