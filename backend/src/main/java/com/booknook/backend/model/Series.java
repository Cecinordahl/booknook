package com.booknook.backend.model;

import com.google.cloud.firestore.annotation.DocumentId;

import java.time.Instant;
import java.time.LocalDate;

public class Series {

    @DocumentId
    private String id;

    private String name;
    private String hardcoverSeriesId;
    private CachedNextRelease cachedNextRelease;

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

    /** Cached result of the last Hardcover lookup, refreshed when {@code fetchedAt} goes stale. */
    public static class CachedNextRelease {
        private String title;
        private LocalDate releaseDate;
        private Instant fetchedAt;

        public CachedNextRelease() {
        }

        public CachedNextRelease(String title, LocalDate releaseDate, Instant fetchedAt) {
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

        public LocalDate getReleaseDate() {
            return releaseDate;
        }

        public void setReleaseDate(LocalDate releaseDate) {
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
