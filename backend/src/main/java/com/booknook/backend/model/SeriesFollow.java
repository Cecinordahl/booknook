package com.booknook.backend.model;

import com.google.cloud.firestore.annotation.DocumentId;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** Document ID is {@code <userUid>_<seriesId>} so follow/unfollow is an idempotent upsert. */
public class SeriesFollow {

    @DocumentId
    private String id;

    private String userUid;
    private String seriesId;
    private Instant followedAt;
    /** Which of the user's configured "days before release" thresholds have already fired for this follow. */
    private List<Integer> notifiedIntervalDays = new ArrayList<>();
    private FollowStatus status = FollowStatus.ACTIVE;

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

    public List<Integer> getNotifiedIntervalDays() {
        return notifiedIntervalDays;
    }

    public void setNotifiedIntervalDays(List<Integer> notifiedIntervalDays) {
        this.notifiedIntervalDays = notifiedIntervalDays;
    }

    public FollowStatus getStatus() {
        return status;
    }

    public void setStatus(FollowStatus status) {
        this.status = status;
    }
}
