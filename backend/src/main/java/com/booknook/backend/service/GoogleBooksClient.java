package com.booknook.backend.service;

import com.booknook.backend.config.BooknookProperties;
import com.booknook.backend.dto.BookMetadataSuggestion;
import com.booknook.backend.exception.ExternalApiException;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriBuilder;
import reactor.util.retry.Retry;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Wraps the Google Books API. An API key ({@code GOOGLE_BOOKS_API_KEY}) is optional — basic
 * lookups work without one — but strongly recommended: the anonymous quota is small and shared
 * across everyone hitting it without a key, so it 429s easily under even light use.
 *
 * <p>Even with a key, Google's backend intermittently returns 5xx errors under normal load
 * (observed in practice, not just theoretical) — every request retries up to twice with a short
 * backoff before giving up, since these tend to be transient blips rather than sustained outages.
 */
@Component
public class GoogleBooksClient {

    private static final Logger log = LoggerFactory.getLogger(GoogleBooksClient.class);
    private static final String BASE_URL = "https://www.googleapis.com/books/v1/volumes";

    private final WebClient webClient;
    private final String apiKey;

    public GoogleBooksClient(WebClient.Builder webClientBuilder, BooknookProperties properties) {
        this.webClient = webClientBuilder.baseUrl(BASE_URL).build();
        this.apiKey = properties.getGoogleBooks().getApiKey();
    }

    /**
     * Retries transient server-side failures (5xx) with a short backoff; deliberately does NOT
     * retry 4xx (a 429 rate-limit retried immediately would just burn more quota and fail again).
     */
    private static final Retry TRANSIENT_FAILURE_RETRY = Retry.backoff(2, Duration.ofMillis(300))
            .filter(GoogleBooksClient::isTransientFailure);

    private static boolean isTransientFailure(Throwable throwable) {
        if (throwable instanceof WebClientResponseException responseException) {
            return responseException.getStatusCode().is5xxServerError();
        }
        return throwable instanceof java.io.IOException;
    }

    private URI withKey(UriBuilder uriBuilder) {
        if (apiKey != null && !apiKey.isBlank()) {
            uriBuilder.queryParam("key", apiKey);
        }
        return uriBuilder.build();
    }

    public Optional<BookMetadataSuggestion> lookupByIsbn(String isbn) {
        try {
            JsonNode response = webClient.get()
                    .uri(uriBuilder -> withKey(uriBuilder.queryParam("q", "isbn:" + isbn)))
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .timeout(Duration.ofSeconds(5))
                    .retryWhen(TRANSIENT_FAILURE_RETRY)
                    .block();

            if (response == null || !response.has("items") || response.get("items").isEmpty()) {
                return Optional.empty();
            }

            JsonNode volumeInfo = response.get("items").get(0).path("volumeInfo");
            return Optional.of(toSuggestion(volumeInfo, isbn));
        } catch (Exception e) {
            log.warn("Google Books lookup failed for ISBN {}: {}", isbn, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Free-text title search for the Add Book search-as-you-type field. Unlike {@link
     * #lookupByIsbn}, failures propagate as {@link ExternalApiException} rather than being
     * swallowed into an empty list — the caller needs to tell "genuinely no matches" (200, empty)
     * apart from "the search backend is down/rate-limited" (error), since this fires on every
     * keystroke and silently doing nothing on failure is confusing for the person typing.
     */
    public List<BookMetadataSuggestion> searchByTitle(String query) {
        try {
            JsonNode response = webClient.get()
                    .uri(uriBuilder -> withKey(uriBuilder
                            .queryParam("q", "intitle:" + query)
                            .queryParam("maxResults", 8)))
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .timeout(Duration.ofSeconds(5))
                    .retryWhen(TRANSIENT_FAILURE_RETRY)
                    .block();

            if (response == null || !response.has("items")) {
                return List.of();
            }

            List<BookMetadataSuggestion> results = new ArrayList<>();
            response.get("items").forEach(item -> results.add(toSuggestion(item.path("volumeInfo"), null)));
            return results;
        } catch (Exception e) {
            log.warn("Google Books title search failed for '{}': {}", query, e.getMessage());
            throw new ExternalApiException("Google Books title search failed", e);
        }
    }

    /**
     * @param isbn the ISBN that was queried for, when known (single-ISBN lookup); pass null to
     *             pull it from the volume's own industryIdentifiers instead (title search, where
     *             several different books/editions come back in one response).
     */
    private BookMetadataSuggestion toSuggestion(JsonNode volumeInfo, String isbn) {
        List<String> authors = new ArrayList<>();
        volumeInfo.path("authors").forEach(a -> authors.add(a.asText()));

        Integer publicationYear = null;
        String publishedDate = volumeInfo.path("publishedDate").asText(null);
        if (publishedDate != null && publishedDate.length() >= 4) {
            try {
                publicationYear = Integer.parseInt(publishedDate.substring(0, 4));
            } catch (NumberFormatException ignored) {
                // malformed date from the API — leave publicationYear unset
            }
        }

        String coverUrl = volumeInfo.path("imageLinks").path("thumbnail").asText(null);
        String resolvedIsbn = isbn != null ? isbn : extractIsbn(volumeInfo);
        // Google's categories are often slash-separated ("Fiction / Fantasy / General") — just the
        // first segment reads more like a genre than the full taxonomy path.
        String genre = volumeInfo.path("categories").isEmpty()
                ? null
                : volumeInfo.get("categories").get(0).asText(null);
        if (genre != null && genre.contains("/")) {
            genre = genre.substring(0, genre.indexOf('/')).trim();
        }

        return new BookMetadataSuggestion(
                volumeInfo.path("title").asText(null),
                authors,
                resolvedIsbn,
                coverUrl,
                volumeInfo.hasNonNull("pageCount") ? volumeInfo.get("pageCount").asInt() : null,
                publicationYear,
                genre,
                "google-books"
        );
    }

    private String extractIsbn(JsonNode volumeInfo) {
        List<JsonNode> identifiers = new ArrayList<>();
        volumeInfo.path("industryIdentifiers").forEach(identifiers::add);

        return identifiers.stream()
                .filter(id -> "ISBN_13".equals(id.path("type").asText()))
                .findFirst()
                .or(() -> identifiers.stream().filter(id -> "ISBN_10".equals(id.path("type").asText())).findFirst())
                .map(id -> id.path("identifier").asText(null))
                .orElse(null);
    }
}
