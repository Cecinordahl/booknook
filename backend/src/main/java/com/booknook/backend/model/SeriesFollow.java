package com.booknook.backend.model;

import com.google.cloud.firestore.annotation.DocumentId;

import java.time.Instant;

/** Document ID is {@code <userUid>_<seriesId>} so follow/unfollow is an idempotent upsert. */
public class SeriesFollow {

    @DocumentId
    private String id;

    private String userUid;
    private String seriesId;
    private Instant followedAt;
    private NotificationStage lastNotifiedStage = NotificationStage.NONE;

    public SeriesFollow() {
    }

    public static String buildId(String userUid, String seriesId) {
        return userUid + "_" + seriesId;
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

    public String getSeriesId() {
        return seriesId;
    }

    public void setSeriesId(String seriesId) {
        this.seriesId = seriesId;
    }

    public Instant getFollowedAt() {
        return followedAt;
    }

    public void setFollowedAt(Instant followedAt) {
        this.followedAt = followedAt;
    }

    public NotificationStage getLastNotifiedStage() {
        return lastNotifiedStage;
    }

    public void setLastNotifiedStage(NotificationStage lastNotifiedStage) {
        this.lastNotifiedStage = lastNotifiedStage;
    }
}
