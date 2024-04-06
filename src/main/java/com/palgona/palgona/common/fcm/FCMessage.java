package com.palgona.palgona.common.fcm;

import com.palgona.palgona.domain.notification.Type;

public record FCMessage(boolean validateOnly, Message message) {

    public record Message(String token, Notification notification, Data data) {
    }

    public record Notification(String title, String body) {
    }

    public record Data(Type type, Long targetId) {
    }
}
