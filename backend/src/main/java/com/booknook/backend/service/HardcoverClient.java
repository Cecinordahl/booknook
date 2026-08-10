package com.booknook.backend.service;

import com.booknook.backend.config.BooknookProperties;
import com.booknook.backend.dto.HardcoverNextRelease;
import com.booknook.backend.dto.HardcoverSeriesMatch;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * GraphQL client for the Hardcover.app API (https://api.hardcover.app/v1/graphql), used for
 * series search and upcoming-release dates.
 *
 * <p><strong>Note:</strong> the exact query/field names below are a best-effort mapping based on
 * Hardcover's publicly documented schema shape (searchable {@code search} root field, a
 * {@code series} type with {@code books}/editions carrying a {@code release_date}). Hardcover's
 * schema can drift; once you have a real API key, sanity-check these queries against the current
 * schema at https://api.hardcover.app and adjust the query strings below if a field has been
 * renamed. Everything that talks to Hardcover funnels through this one class for exactly that
 * reason.
 */
@Component
public class HardcoverClient {

    private static final Logger log = LoggerFactory.getLogger(HardcoverClient.class);

    private static final String SEARCH_SERIES_QUERY = """
            query SearchSeries($query: String!) {
              search(query: $query, query_type: "series") {
                results
              }
            }
            """;

    private static final String NEXT_RELEASE_QUERY = """
            query SeriesNextRelease($seriesId: Int!) {
              series(id: $seriesId) {
                id
                name
                books(order_by: {release_date: asc}, where: {release_date: {_gt: "now()"}}, limit: 1) {
                  title
                  release_date
                }
              }
            }
            """;

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

    public List<HardcoverSeriesMatch> searchSeries(String query) {
        if (!configured) {
            log.warn("HARDCOVER_API_KEY not set — series search returns no results.");
            return List.of();
        }
        try {
            JsonNode response = execute(SEARCH_SERIES_QUERY, Map.of("query", query));
            JsonNode results = response.path("data").path("search").path("results");

            List<HardcoverSeriesMatch> matches = new ArrayList<>();
            results.forEach(hit -> matches.add(new HardcoverSeriesMatch(
                    hit.path("id").asText(),
                    hit.path("name").asText()
            )));
            return matches;
        } catch (Exception e) {
            log.warn("Hardcover series search failed for '{}': {}", query, e.getMessage());
            return List.of();
        }
    }

    public Optional<HardcoverNextRelease> getNextRelease(String hardcoverSeriesId) {
        if (!configured) {
            return Optional.empty();
        }
        try {
            JsonNode response = execute(NEXT_RELEASE_QUERY, Map.of("seriesId", Integer.parseInt(hardcoverSeriesId)));
            JsonNode books = response.path("data").path("series").path("books");
            if (!books.isArray() || books.isEmpty()) {
                return Optional.empty();
            }
            JsonNode next = books.get(0);
            String dateStr = next.path("release_date").asText(null);
            if (dateStr == null) {
                return Optional.empty();
            }
            return Optional.of(new HardcoverNextRelease(next.path("title").asText(), LocalDate.parse(dateStr)));
        } catch (Exception e) {
            log.warn("Hardcover next-release lookup failed for series {}: {}", hardcoverSeriesId, e.getMessage());
            return Optional.empty();
        }
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
