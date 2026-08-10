package com.booknook.backend.model;

import com.google.cloud.firestore.annotation.DocumentId;

import java.time.Instant;

public class PushSubscription {

    @DocumentId
    private String id;

    private String userUid;
    private String endpoint;
    private Keys keys;
    private Instant createdAt;

    public PushSubscription() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserUid() {
        return userUid;
    }

    public void setUserUid(String userUid) {
        this.userUid = userUid;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public Keys getKeys() {
        return keys;
    }

    public void setKeys(Keys keys) {
        this.keys = keys;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    /** Matches the shape of the browser PushSubscription.toJSON().keys object. */
    public static class Keys {
        private String p256dh;
        private String auth;

        public Keys() {
        }

        public Keys(String p256dh, String auth) {
            this.p256dh = p256dh;
            this.auth = auth;
        }

        public String getP256dh() {
            return p256dh;
        }

        public void setP256dh(String p256dh) {
            this.p256dh = p256dh;
        }

        public String getAuth() {
            return auth;
        }

        public void setAuth(String auth) {
            this.auth = auth;
        }
    }
}
