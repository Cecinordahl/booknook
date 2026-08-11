package com.booknook.backend.dto;

import com.booknook.backend.model.BookFormat;
import com.booknook.backend.model.BookStatus;

import java.util.List;

/**
 * Query parameters for {@code GET /api/books}. Firestore only allows a range filter (>, <, >=,
 * <=) on a single field per query, so at most one of {@code minPageCount/maxPageCount},
 * {@code minPublicationYear/maxPublicationYear}, or {@code minRating} may be set at a time —
 * {@code BookRepository} picks whichever one is present, in that priority order, and applies the
 * rest as in-memory filtering after the fetch. Fine at this app's scale (a personal library, not
 * a public catalog).
 */
public class BookFilter {

    private String genre;
    private String source;
    private List<String> moodTags;
    private BookStatus status;
    private BookFormat format;
    private Integer minPageCount;
    private Integer maxPageCount;
    private Integer minPublicationYear;
    private Integer maxPublicationYear;
    private Double minRating;
    private String sortBy = "addedAt";
    private boolean sortDescending = true;

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public List<String> getMoodTags() {
        return moodTags;
    }

    public void setMoodTags(List<String> moodTags) {
        this.moodTags = moodTags;
    }

    public BookStatus getStatus() {
        return status;
    }

    public void setStatus(BookStatus status) {
        this.status = status;
    }

    public BookFormat getFormat() {
        return format;
    }

    public void setFormat(BookFormat format) {
        this.format = format;
    }

    public Integer getMinPageCount() {
        return minPageCount;
    }

    public void setMinPageCount(Integer minPageCount) {
        this.minPageCount = minPageCount;
    }

    public Integer getMaxPageCount() {
        return maxPageCount;
    }

    public void setMaxPageCount(Integer maxPageCount) {
        this.maxPageCount = maxPageCount;
    }

    public Integer getMinPublicationYear() {
        return minPublicationYear;
    }

    public void setMinPublicationYear(Integer minPublicationYear) {
        this.minPublicationYear = minPublicationYear;
    }

    public Integer getMaxPublicationYear() {
        return maxPublicationYear;
    }

    public void setMaxPublicationYear(Integer maxPublicationYear) {
        this.maxPublicationYear = maxPublicationYear;
    }

    public Double getMinRating() {
        return minRating;
    }

    public void setMinRating(Double minRating) {
        this.minRating = minRating;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public boolean isSortDescending() {
        return sortDescending;
    }

    public void setSortDescending(boolean sortDescending) {
        this.sortDescending = sortDescending;
    }
}
