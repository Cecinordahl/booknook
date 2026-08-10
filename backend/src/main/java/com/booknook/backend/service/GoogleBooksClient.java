package com.booknook.backend.service;

import com.booknook.backend.dto.BookMetadataSuggestion;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** No API key required for basic volume lookups. */
@Component
public class GoogleBooksClient {

    private static final Logger log = LoggerFactory.getLogger(GoogleBooksClient.class);
    private static final String BASE_URL = "https://www.googleapis.com/books/v1/volumes";

    private final WebClient webClient;

    public GoogleBooksClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl(BASE_URL).build();
    }

    public Optional<BookMetadataSuggestion> lookupByIsbn(String isbn) {
        try {
            JsonNode response = webClient.get()
                    .uri(uriBuilder -> uriBuilder.queryParam("q", "isbn:" + isbn).build())
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .timeout(Duration.ofSeconds(5))
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

        return new BookMetadataSuggestion(
                volumeInfo.path("title").asText(null),
                authors,
                isbn,
                coverUrl,
                volumeInfo.hasNonNull("pageCount") ? volumeInfo.get("pageCount").asInt() : null,
                publicationYear,
                "google-books"
        );
    }
}
