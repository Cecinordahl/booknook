package com.booknook.backend.dto;

import java.util.List;

/**
 * A best-effort metadata match for an ISBN or title/author search, returned to the frontend as a
 * suggestion. The user always confirms/edits before it's saved as a {@code Book} — this is never
 * written to Firestore directly.
 */
public class BookMetadataSuggestion {

    private String title;
    private List<String> authors;
    private String isbn;
    private String coverImageUrl;
    private Integer pageCount;
    private Integer publicationYear;
    private String source;

    public BookMetadataSuggestion() {
    }

    public BookMetadataSuggestion(String title, List<String> authors, String isbn, String coverImageUrl,
                                   Integer pageCount, Integer publicationYear, String source) {
        this.title = title;
        this.authors = authors;
        this.isbn = isbn;
        this.coverImageUrl = coverImageUrl;
        this.pageCount = pageCount;
        this.publicationYear = publicationYear;
        this.source = source;
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

    public Integer getPublicationYear() {
        return publicationYear;
    }

    public void setPublicationYear(Integer publicationYear) {
        this.publicationYear = publicationYear;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
