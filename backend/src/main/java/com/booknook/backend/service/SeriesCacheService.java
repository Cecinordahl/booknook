package com.booknook.backend.service;

import com.booknook.backend.config.BooknookProperties;
import com.booknook.backend.dto.HardcoverNextRelease;
import com.booknook.backend.model.Series;
import com.booknook.backend.repository.SeriesRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

/**
 * Keeps {@link Series#getCachedNextRelease()} reasonably fresh without hitting Hardcover on
 * every request — refetches only when the cached value is older than the configured TTL.
 */
@Service
public class SeriesCacheService {

    private static final Logger log = LoggerFactory.getLogger(SeriesCacheService.class);

    private final SeriesRepository seriesRepository;
    private final HardcoverClient hardcoverClient;
    private final Duration ttl;

    public SeriesCacheService(SeriesRepository seriesRepository, HardcoverClient hardcoverClient,
                               BooknookProperties properties) {
        this.seriesRepository = seriesRepository;
        this.hardcoverClient = hardcoverClient;
        this.ttl = Duration.ofHours(properties.getSeriesCache().getTtlHours());
    }

    public Series refreshIfStale(Series series) throws ExecutionException, InterruptedException {
        Series.CachedNextRelease cached = series.getCachedNextRelease();
        boolean stale = cached == null || cached.getFetchedAt() == null
                || Duration.between(cached.getFetchedAt(), Instant.now()).compareTo(ttl) > 0;

        if (!stale) {
            return series;
        }

        Optional<HardcoverNextRelease> next = hardcoverClient.getNextRelease(series.getHardcoverSeriesId());
        Series.CachedNextRelease updated = next
                .map(r -> new Series.CachedNextRelease(r.title(), r.releaseDate(), Instant.now()))
                .orElse(new Series.CachedNextRelease(null, null, Instant.now()));

        series.setCachedNextRelease(updated);
        seriesRepository.save(series);
        log.debug("Refreshed release-date cache for series {}", series.getId());
        return series;
    }
}
