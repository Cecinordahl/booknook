package com.booknook.backend.model;

import com.google.cloud.firestore.annotation.DocumentId;

import java.time.Instant;
import java.util.List;

/** Document ID is the Firebase Auth UID. */
public class UserAccount {

    @DocumentId
    private String uid;

    private String email;
    private String displayName;
    private Instant createdAt;
    /** Null until the user saves their own choice — {@link com.booknook.backend.service.UserAccountService} fills in a default at read time. */
    private List<Integer> notificationIntervalDays;

    public UserAccount() {
    }

    public UserAccount(String uid, String email, String displayName, Instant createdAt) {
        this.uid = uid;
        this.email = email;
        this.displayName = displayName;
        this.createdAt = createdAt;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public List<Integer> getNotificationIntervalDays() {
        return notificationIntervalDays;
    }

    public void setNotificationIntervalDays(List<Integer> notificationIntervalDays) {
        this.notificationIntervalDays = notificationIntervalDays;
    }
}
