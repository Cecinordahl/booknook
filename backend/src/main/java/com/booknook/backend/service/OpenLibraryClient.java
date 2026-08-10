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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Fallback for when a book isn't in Google Books' catalog. No API key required. */
@Component
public class OpenLibraryClient {

    private static final Logger log = LoggerFactory.getLogger(OpenLibraryClient.class);
    private static final String BASE_URL = "https://openlibrary.org";
    private static final Pattern YEAR_PATTERN = Pattern.compile("(\\d{4})");

    private final WebClient webClient;

    public OpenLibraryClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl(BASE_URL).build();
    }

    public Optional<BookMetadataSuggestion> lookupByIsbn(String isbn) {
        String bibkey = "ISBN:" + isbn;
        try {
            JsonNode response = webClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/api/books")
                            .queryParam("bibkeys", bibkey)
                            .queryParam("format", "json")
                            .queryParam("jscmd", "data")
                            .build())
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();

            if (response == null || !response.has(bibkey)) {
                return Optional.empty();
            }

            return Optional.of(toSuggestion(response.get(bibkey), isbn));
        } catch (Exception e) {
            log.warn("Open Library lookup failed for ISBN {}: {}", isbn, e.getMessage());
            return Optional.empty();
        }
    }

    private BookMetadataSuggestion toSuggestion(JsonNode data, String isbn) {
        List<String> authors = new ArrayList<>();
        data.path("authors").forEach(a -> authors.add(a.path("name").asText()));

        Integer publicationYear = null;
        String publishDate = data.path("publish_date").asText(null);
        if (publishDate != null) {
            Matcher matcher = YEAR_PATTERN.matcher(publishDate);
            if (matcher.find()) {
                publicationYear = Integer.parseInt(matcher.group(1));
            }
        }

        String coverUrl = data.path("cover").path("medium").asText(null);
        String genre = data.path("subjects").isEmpty() ? null : data.get("subjects").get(0).path("name").asText(null);

        return new BookMetadataSuggestion(
                data.path("title").asText(null),
                authors,
                isbn,
                coverUrl,
                data.hasNonNull("number_of_pages") ? data.get("number_of_pages").asInt() : null,
                publicationYear,
                genre,
                "open-library"
        );
    }
}
