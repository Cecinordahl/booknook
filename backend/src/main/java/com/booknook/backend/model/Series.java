package com.booknook.backend.model;

import com.google.cloud.firestore.annotation.DocumentId;

import java.time.Instant;

public class Series {

    @DocumentId
    private String id;

    private String name;
    private String hardcoverSeriesId;
    private CachedNextRelease cachedNextRelease;
    /** Null until the first cache refresh — Hardcover doesn't always have this set either. */
    private Boolean isCompleted;

    public Series() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHardcoverSeriesId() {
        return hardcoverSeriesId;
    }

    public void setHardcoverSeriesId(String hardcoverSeriesId) {
        this.hardcoverSeriesId = hardcoverSeriesId;
    }

    public CachedNextRelease getCachedNextRelease() {
        return cachedNextRelease;
    }

    public void setCachedNextRelease(CachedNextRelease cachedNextRelease) {
        this.cachedNextRelease = cachedNextRelease;
    }

    public Boolean getIsCompleted() {
        return isCompleted;
    }

    public void setIsCompleted(Boolean isCompleted) {
        this.isCompleted = isCompleted;
    }

    /**
     * Cached result of the last Hardcover lookup, refreshed when {@code fetchedAt} goes stale.
     *
     * <p>{@code releaseDate} is stored as an ISO-8601 string ("2026-12-01"), not a
     * {@code java.time.LocalDate} — the Firestore Java SDK's POJO mapper can't serialize
     * {@code LocalDate} (throws "Found conflicting getters for name getEra", a reflection
     * ambiguity in its bean-mapping code, not something we can annotate around). Callers parse
     * with {@code LocalDate.parse(...)} at the boundary where they need date arithmetic.
     */
    public static class CachedNextRelease {
        private String title;
        private String releaseDate;
        private Instant fetchedAt;

        public CachedNextRelease() {
        }

        public CachedNextRelease(String title, String releaseDate, Instant fetchedAt) {
            this.title = title;
            this.releaseDate = releaseDate;
            this.fetchedAt = fetchedAt;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getReleaseDate() {
            return releaseDate;
        }

        public void setReleaseDate(String releaseDate) {
            this.releaseDate = releaseDate;
        }

        public Instant getFetchedAt() {
            return fetchedAt;
        }

        public void setFetchedAt(Instant fetchedAt) {
            this.fetchedAt = fetchedAt;
        }
    }
}
