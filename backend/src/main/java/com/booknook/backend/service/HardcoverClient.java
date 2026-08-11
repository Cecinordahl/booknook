package com.booknook.backend.service;

import com.booknook.backend.config.BooknookProperties;
import com.booknook.backend.dto.HardcoverBookSeriesMatch;
import com.booknook.backend.dto.HardcoverSeriesBook;
import com.booknook.backend.dto.HardcoverSeriesStatus;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * GraphQL client for the Hardcover.app API (https://api.hardcover.app/v1/graphql): resolving a
 * book's series by ISBN, release/completion status for a series, and the full book list for a
 * series.
 *
 * <p>The queries below are verified against Hardcover's live schema (confirmed via introspection
 * + real requests with an API key) — not a guess. Notable shapes: the series root query is
 * {@code series_by_pk(id: ...)}, with books reached through the {@code book_series} join field
 * rather than a direct {@code books} field on {@code series}; a book's series is reached from
 * {@code editions} (which carry ISBNs) via {@code book.featured_book_series}; {@code is_completed}
 * is frequently {@code null} rather than {@code false} for series Hardcover doesn't have reliable
 * data on; and {@code book_series} has heavy duplication per position (foreign-language editions,
 * box sets, dramatized adaptations all show up as separate "books" at the same position) — worth
 * filtering, not fixable via query params.
 */
@Component
public class HardcoverClient {

    private static final Logger log = LoggerFactory.getLogger(HardcoverClient.class);

    private static final String SERIES_STATUS_QUERY = """
            query SeriesStatus($seriesId: Int!, $afterDate: date!) {
              series_by_pk(id: $seriesId) {
                id
                name
                is_completed
                book_series(
                  order_by: {book: {release_date: asc}}
                  where: {book: {release_date: {_gt: $afterDate}}}
                  limit: 1
                ) {
                  book {
                    title
                    release_date
                  }
                }
              }
            }
            """;

    private static final String SERIES_FOR_ISBN_QUERY = """
            query SeriesForIsbn($isbn: String!) {
              editions(where: {isbn_13: {_eq: $isbn}}, limit: 1) {
                book {
                  featured_book_series {
                    position
                    series {
                      id
                      name
                    }
                  }
                }
              }
            }
            """;

    private static final String SERIES_BOOKS_QUERY = """
            query SeriesBooks($seriesId: Int!) {
              series_by_pk(id: $seriesId) {
                book_series(order_by: {position: asc}) {
                  position
                  book {
                    title
                    release_date
                    cached_image
                  }
                }
              }
            }
            """;

    /** Titles matching these (case-insensitive) are compilations/adaptations, not the primary book at a position. */
    private static final List<String> NON_PRIMARY_TITLE_MARKERS =
            List.of("set", "collection", "box", "dramatized", "adaptation", "bundle");

    private final WebClient webClient;
    private final boolean configured;

    public HardcoverClient(WebClient.Builder webClientBuilder, BooknookProperties properties) {
        String apiKey = properties.getHardcover().getApiKey();
        this.configured = apiKey != null && !apiKey.isBlank();
        this.webClient = webClientBuilder
                .baseUrl(properties.getHardcover().getGraphqlUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .build();
    }

    public Optional<HardcoverSeriesStatus> getSeriesStatus(String hardcoverSeriesId) {
        if (!configured) {
            return Optional.empty();
        }
        try {
            JsonNode response = execute(SERIES_STATUS_QUERY, Map.of(
                    "seriesId", Integer.parseInt(hardcoverSeriesId),
                    "afterDate", LocalDate.now().toString()
            ));
            JsonNode seriesNode = response.path("data").path("series_by_pk");
            if (seriesNode.isMissingNode() || seriesNode.isNull()) {
                return Optional.empty();
            }

            boolean isCompleted = seriesNode.path("is_completed").asBoolean(false);
            JsonNode bookSeries = seriesNode.path("book_series");

            String nextTitle = null;
            LocalDate nextDate = null;
            if (bookSeries.isArray() && !bookSeries.isEmpty()) {
                JsonNode book = bookSeries.get(0).path("book");
                String dateStr = book.path("release_date").asText(null);
                if (dateStr != null) {
                    nextTitle = book.path("title").asText();
                    nextDate = LocalDate.parse(dateStr);
                }
            }

            return Optional.of(new HardcoverSeriesStatus(nextTitle, nextDate, isCompleted));
        } catch (Exception e) {
            log.warn("Hardcover series-status lookup failed for series {}: {}", hardcoverSeriesId, e.getMessage());
            return Optional.empty();
        }
    }

    /** Looks up which series (if any) a book belongs to, via its ISBN-13. */
    public Optional<HardcoverBookSeriesMatch> findSeriesForIsbn(String isbn) {
        if (!configured || isbn == null || isbn.isBlank()) {
            return Optional.empty();
        }
        try {
            JsonNode response = execute(SERIES_FOR_ISBN_QUERY, Map.of("isbn", isbn));
            JsonNode editions = response.path("data").path("editions");
            if (!editions.isArray() || editions.isEmpty()) {
                return Optional.empty();
            }

            JsonNode featured = editions.get(0).path("book").path("featured_book_series");
            if (featured.isMissingNode() || featured.isNull()) {
                return Optional.empty();
            }

            JsonNode series = featured.path("series");
            Double position = featured.hasNonNull("position") ? featured.get("position").asDouble() : null;
            return Optional.of(new HardcoverBookSeriesMatch(series.path("id").asText(), series.path("name").asText(), position));
        } catch (Exception e) {
            log.warn("Hardcover series-for-ISBN lookup failed for {}: {}", isbn, e.getMessage());
            return Optional.empty();
        }
    }

    /** Full book list for a series, deduped down to (roughly) one primary edition per position. */
    public List<HardcoverSeriesBook> listSeriesBooks(String hardcoverSeriesId) {
        if (!configured) {
            return List.of();
        }
        try {
            JsonNode response = execute(SERIES_BOOKS_QUERY, Map.of("seriesId", Integer.parseInt(hardcoverSeriesId)));
            JsonNode bookSeries = response.path("data").path("series_by_pk").path("book_series");
            if (!bookSeries.isArray()) {
                return List.of();
            }

            // Keep the first non-"obviously secondary" entry per position; if every entry at a
            // position looks secondary (box set, adaptation, ...), fall back to the first one
            // rather than dropping the position entirely.
            Map<Double, HardcoverSeriesBook> byPosition = new LinkedHashMap<>();
            for (JsonNode entry : bookSeries) {
                Double position = entry.hasNonNull("position") ? entry.get("position").asDouble() : null;
                JsonNode book = entry.path("book");
                String title = book.path("title").asText("");
                boolean looksSecondary = NON_PRIMARY_TITLE_MARKERS.stream()
                        .anyMatch(marker -> title.toLowerCase(Locale.ROOT).contains(marker));

                if (byPosition.containsKey(position) && looksSecondary) {
                    continue;
                }
                if (byPosition.containsKey(position) && !looksSecondary
                        && !isSecondary(byPosition.get(position).title())) {
                    continue; // already have a good primary candidate for this position
                }

                String dateStr = book.path("release_date").asText(null);
                LocalDate releaseDate = dateStr != null ? LocalDate.parse(dateStr) : null;
                String coverUrl = book.path("cached_image").path("url").asText(null);
                byPosition.put(position, new HardcoverSeriesBook(title, coverUrl, releaseDate, position));
            }

            List<HardcoverSeriesBook> books = new ArrayList<>(byPosition.values());
            books.sort((a, b) -> {
                if (a.position() == null) return 1;
                if (b.position() == null) return -1;
                return Double.compare(a.position(), b.position());
            });
            return books;
        } catch (Exception e) {
            log.warn("Hardcover series-books lookup failed for series {}: {}", hardcoverSeriesId, e.getMessage());
            return List.of();
        }
    }

    private boolean isSecondary(String title) {
        String lower = title.toLowerCase(Locale.ROOT);
        return NON_PRIMARY_TITLE_MARKERS.stream().anyMatch(lower::contains);
    }

    private JsonNode execute(String query, Map<String, Object> variables) {
        return webClient.post()
                .bodyValue(Map.of("query", query, "variables", variables))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .timeout(Duration.ofSeconds(8))
                .block();
    }
}
