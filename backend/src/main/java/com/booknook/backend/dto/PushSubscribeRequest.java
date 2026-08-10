package com.booknook.backend.dto;

/** Mirrors the shape of the browser's PushSubscription.toJSON() output. */
public record PushSubscribeRequest(String endpoint, Keys keys) {
    public record Keys(String p256dh, String auth) {
    }
}
