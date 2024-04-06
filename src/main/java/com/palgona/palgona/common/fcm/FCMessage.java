package com.palgona.palgona.common.fcm;

public record FCMessage(boolean validateOnly, Message message) {

    public record Message(String token, Notification notification, Data data) {
    }

    public record Notification(String title, String body) {
    }

    public record Data(String url) {
    }
}
