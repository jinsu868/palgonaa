package com.palgona.palgona.dto;

import com.palgona.palgona.domain.notification.Notification;

public record NotificationResponse(
        Long id,
        String title,
        String body,
        String targetUrl){

    public static NotificationResponse from(Notification notification){
        return new NotificationResponse(
                notification.getId(),
                notification.getTitle(),
                notification.getBody(),
                notification.getTargetUrl()
        );
    }
}
