package com.booknook.backend.service;

import com.booknook.backend.dto.BookMetadataSuggestion;
import com.booknook.backend.model.IsbnLookupCache;
import com.booknook.backend.repository.IsbnLookupCacheRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

/**
 * Resolves an ISBN (from a barcode scan or manual entry) to book metadata: Firestore cache first,
 * then Google Books, then Open Library as a fallback for titles Google doesn't have.
 */
@Service
public class BookLookupService {

    private static final Logger log = LoggerFactory.getLogger(BookLookupService.class);

    private final GoogleBooksClient googleBooksClient;
    private final OpenLibraryClient openLibraryClient;
    private final IsbnLookupCacheRepository cacheRepository;

    public BookLookupService(GoogleBooksClient googleBooksClient, OpenLibraryClient openLibraryClient,
                              IsbnLookupCacheRepository cacheRepository) {
        this.googleBooksClient = googleBooksClient;
        this.openLibraryClient = openLibraryClient;
        this.cacheRepository = cacheRepository;
    }

    public Optional<BookMetadataSuggestion> lookupByIsbn(String isbn) {
        String normalized = isbn.replaceAll("[^0-9Xx]", "");

        try {
            Optional<IsbnLookupCache> cached = cacheRepository.findByIsbn(normalized);
            if (cached.isPresent()) {
                return Optional.of(cached.get().getSuggestion());
            }
        } catch (ExecutionException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            log.warn("ISBN cache read failed, falling back to live lookup: {}", e.getMessage());
        }

        Optional<BookMetadataSuggestion> result = googleBooksClient.lookupByIsbn(normalized)
                .or(() -> openLibraryClient.lookupByIsbn(normalized));

        result.ifPresent(suggestion -> {
            try {
                cacheRepository.save(new IsbnLookupCache(normalized, suggestion, Instant.now()));
            } catch (ExecutionException | InterruptedException e) {
                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
                log.warn("Failed to cache ISBN lookup for {}: {}", normalized, e.getMessage());
            }
        });

        return result;
    }

    /**
     * Search-as-you-type for the Add Book form's title field. Unlike ISBN lookup, this isn't
     * cached (free-text queries have low cache-hit value) and doesn't fall back to Open Library
     * (Google Books' free-text search covers this well enough for a first pass).
     */
    public List<BookMetadataSuggestion> searchByTitle(String query) {
        return googleBooksClient.searchByTitle(query);
    }
}
