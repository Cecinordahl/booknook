package com.booknook.backend.service;

import com.booknook.backend.config.BooknookProperties;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import static org.junit.jupiter.api.Assertions.assertTrue;

class HardcoverClientTest {

    @Test
    void returnsNoResultsWithoutMakingARequestWhenApiKeyIsMissing() {
        BooknookProperties properties = new BooknookProperties();
        properties.getHardcover().setApiKey(""); // not configured
        properties.getHardcover().setGraphqlUrl("https://api.hardcover.app/v1/graphql");

        HardcoverClient client = new HardcoverClient(WebClient.builder(), properties);

        assertTrue(client.getSeriesStatus("123").isEmpty());
        assertTrue(client.findSeriesForIsbn("9781649374042").isEmpty());
        assertTrue(client.listSeriesBooks("123").isEmpty());
    }
}
