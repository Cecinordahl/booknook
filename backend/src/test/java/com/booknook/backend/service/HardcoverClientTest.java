package com.booknook.backend.service;

import com.booknook.backend.config.BooknookProperties;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import static org.junit.jupiter.api.Assertions.assertTrue;

class HardcoverClientTest {

    @Test
    void searchReturnsNoResultsWithoutMakingARequestWhenApiKeyIsMissing() {
        BooknookProperties properties = new BooknookProperties();
        properties.getHardcover().setApiKey(""); // not configured
        properties.getHardcover().setGraphqlUrl("https://api.hardcover.app/v1/graphql");

        HardcoverClient client = new HardcoverClient(WebClient.builder(), properties);

        assertTrue(client.searchSeries("Mistborn").isEmpty());
        assertTrue(client.getNextRelease("123").isEmpty());
    }
}
