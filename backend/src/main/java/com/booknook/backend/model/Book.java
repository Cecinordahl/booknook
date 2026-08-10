package com.booknook.backend.model;

import com.google.cloud.firestore.annotation.DocumentId;

import java.time.Instant;
import java.util.List;

public class Book {

    @DocumentId
    private String id;

    private String ownerUid;
    private String title;
    private List<String> authors;
    private String isbn;
    private String coverImageUrl;

    private Integer pageCount;
    private Integer currentPage;
    private BookFormat format;
    private BookStatus status;

    private String genre;
    private List<String> moodTags;
    private Integer publicationYear;
    private Double personalRating;

    private String seriesId;
    private Integer seriesPosition;

    /** ID of the matching work in Hardcover's catalog, if one was found — used to resolve series data. */
    private String hardcoverBookId;

    private Instant addedAt;
    private Instant updatedAt;

    public Book() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOwnerUid() {
        return ownerUid;
    }

    public void setOwnerUid(String ownerUid) {
        this.ownerUid = ownerUid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getAuthors() {
        return authors;
    }

    public void setAuthors(List<String> authors) {
        this.authors = authors;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }

    public Integer getPageCount() {
        return pageCount;
    }

    public void setPageCount(Integer pageCount) {
        this.pageCount = pageCount;
    }

    public Integer getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(Integer currentPage) {
        this.currentPage = currentPage;
    }

    public BookFormat getFormat() {
        return format;
    }

    public void setFormat(BookFormat format) {
        this.format = format;
    }

    public BookStatus getStatus() {
        return status;
    }

    public void setStatus(BookStatus status) {
        this.status = status;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public List<String> getMoodTags() {
        return moodTags;
    }

    public void setMoodTags(List<String> moodTags) {
        this.moodTags = moodTags;
    }

    public Integer getPublicationYear() {
        return publicationYear;
    }

    public void setPublicationYear(Integer publicationYear) {
        this.publicationYear = publicationYear;
    }

    public Double getPersonalRating() {
        return personalRating;
    }

    public void setPersonalRating(Double personalRating) {
        this.personalRating = personalRating;
    }

    public String getSeriesId() {
        return seriesId;
    }

    public void setSeriesId(String seriesId) {
        this.seriesId = seriesId;
    }

    public Integer getSeriesPosition() {
        return seriesPosition;
    }

    public void setSeriesPosition(Integer seriesPosition) {
        this.seriesPosition = seriesPosition;
    }

    public String getHardcoverBookId() {
        return hardcoverBookId;
    }

    public void setHardcoverBookId(String hardcoverBookId) {
        this.hardcoverBookId = hardcoverBookId;
    }

    public Instant getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(Instant addedAt) {
        this.addedAt = addedAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
