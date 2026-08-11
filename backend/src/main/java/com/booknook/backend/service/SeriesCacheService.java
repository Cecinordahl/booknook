package com.booknook.backend.service;

import com.booknook.backend.config.BooknookProperties;
import com.booknook.backend.dto.HardcoverSeriesStatus;
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
 * Keeps {@link Series#getCachedNextRelease()} (and completion status) reasonably fresh without
 * hitting Hardcover on every request — refetches only when the cached value is older than the
 * configured TTL.
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

        Optional<HardcoverSeriesStatus> status = hardcoverClient.getSeriesStatus(series.getHardcoverSeriesId());
        Series.CachedNextRelease updated = status
                .map(s -> new Series.CachedNextRelease(
                        s.nextReleaseTitle(),
                        s.nextReleaseDate() != null ? s.nextReleaseDate().toString() : null,
                        Instant.now()))
                .orElse(new Series.CachedNextRelease(null, null, Instant.now()));

        series.setCachedNextRelease(updated);
        series.setIsCompleted(status.map(HardcoverSeriesStatus::isCompleted).orElse(null));
        seriesRepository.save(series);
        log.debug("Refreshed release-date cache for series {}", series.getId());
        return series;
    }
}
