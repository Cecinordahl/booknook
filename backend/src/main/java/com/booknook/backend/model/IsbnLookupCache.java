package com.booknook.backend.model;

import com.booknook.backend.dto.BookMetadataSuggestion;
import com.google.cloud.firestore.annotation.DocumentId;

import java.time.Instant;

/** Caches ISBN -> metadata lookups so Google Books / Open Library aren't hit on every scan. Document ID is the ISBN. */
public class IsbnLookupCache {

    @DocumentId
    private String isbn;

    private BookMetadataSuggestion suggestion;
    private Instant fetchedAt;

    public IsbnLookupCache() {
    }

    public IsbnLookupCache(String isbn, BookMetadataSuggestion suggestion, Instant fetchedAt) {
        this.isbn = isbn;
        this.suggestion = suggestion;
        this.fetchedAt = fetchedAt;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public BookMetadataSuggestion getSuggestion() {
        return suggestion;
    }

    public void setSuggestion(BookMetadataSuggestion suggestion) {
        this.suggestion = suggestion;
    }

    public Instant getFetchedAt() {
        return fetchedAt;
    }

    public void setFetchedAt(Instant fetchedAt) {
        this.fetchedAt = fetchedAt;
    }
}
