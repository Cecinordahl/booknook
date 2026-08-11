package com.booknook.backend.service;

import com.booknook.backend.dto.HardcoverSeriesBook;
import com.booknook.backend.dto.SeriesFollowView;
import com.booknook.backend.exception.ResourceNotFoundException;
import com.booknook.backend.model.FollowStatus;
import com.booknook.backend.model.Series;
import com.booknook.backend.model.SeriesFollow;
import com.booknook.backend.repository.SeriesFollowRepository;
import com.booknook.backend.repository.SeriesRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Service
public class SeriesService {

    private final SeriesRepository seriesRepository;
    private final SeriesFollowRepository seriesFollowRepository;
    private final SeriesCacheService seriesCacheService;
    private final HardcoverClient hardcoverClient;

    public SeriesService(SeriesRepository seriesRepository, SeriesFollowRepository seriesFollowRepository,
                          SeriesCacheService seriesCacheService, HardcoverClient hardcoverClient) {
        this.seriesRepository = seriesRepository;
        this.seriesFollowRepository = seriesFollowRepository;
        this.seriesCacheService = seriesCacheService;
        this.hardcoverClient = hardcoverClient;
    }

    public Series resolveOrCreateSeries(String hardcoverSeriesId, String name) throws ExecutionException, InterruptedException {
        Optional<Series> existing = seriesRepository.findByHardcoverSeriesId(hardcoverSeriesId);
        if (existing.isPresent()) {
            return existing.get();
        }
        Series series = new Series();
        series.setHardcoverSeriesId(hardcoverSeriesId);
        series.setName(name);
        return seriesRepository.save(series);
    }

    /**
     * Called when a book with a resolved series is added. Creates an ACTIVE follow if the user
     * has never followed this series before; leaves an existing DISCARDED follow alone (adding
     * another book from a series you already opted out of shouldn't silently re-follow it); is a
     * no-op if already ACTIVE. Always returns the {@link Series} so the caller can tag the book.
     */
    public Series autoAttachSeriesForBook(String userUid, String hardcoverSeriesId, String seriesName)
            throws ExecutionException, InterruptedException {
        Series series = resolveOrCreateSeries(hardcoverSeriesId, seriesName);

        Optional<SeriesFollow> existingFollow = seriesFollowRepository.findByUserAndSeries(userUid, series.getId());
        if (existingFollow.isEmpty()) {
            SeriesFollow follow = new SeriesFollow();
            follow.setUserUid(userUid);
            follow.setSeriesId(series.getId());
            follow.setFollowedAt(Instant.now());
            follow.setStatus(FollowStatus.ACTIVE);
            seriesFollowRepository.save(follow);
        }
        return series;
    }

    public void unfollow(String userUid, String seriesId) throws ExecutionException, InterruptedException {
        seriesFollowRepository.findByUserAndSeries(userUid, seriesId).ifPresentOrElse(follow -> {
            follow.setStatus(FollowStatus.DISCARDED);
            try {
                seriesFollowRepository.save(follow);
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException("Failed to discard follow for series " + seriesId, e);
            }
        }, () -> {
            throw new ResourceNotFoundException("Not following series: " + seriesId);
        });
    }

    public void reactivate(String userUid, String seriesId) throws ExecutionException, InterruptedException {
        SeriesFollow follow = seriesFollowRepository.findByUserAndSeries(userUid, seriesId)
                .orElseThrow(() -> new ResourceNotFoundException("No follow record for series: " + seriesId));
        follow.setStatus(FollowStatus.ACTIVE);
        seriesFollowRepository.save(follow);
    }

    public List<SeriesFollowView> listFollowed(String userUid) throws ExecutionException, InterruptedException {
        List<SeriesFollow> follows = seriesFollowRepository.findByUser(userUid);
        List<SeriesFollowView> views = new ArrayList<>();

        for (SeriesFollow follow : follows) {
            Optional<Series> seriesOpt = seriesRepository.findById(follow.getSeriesId());
            if (seriesOpt.isEmpty()) {
                continue;
            }
            boolean discarded = follow.getStatus() == FollowStatus.DISCARDED;
            // Only spend a Hardcover call refreshing release/completion data for series the user
            // is actively following — a discarded entry just needs its name for the list.
            Series series = discarded ? seriesOpt.get() : seriesCacheService.refreshIfStale(seriesOpt.get());
            views.add(toView(series, discarded));
        }

        return views;
    }

    public SeriesFollowView getSeriesDetail(String userUid, String seriesId) throws ExecutionException, InterruptedException {
        Series series = seriesRepository.findById(seriesId)
                .orElseThrow(() -> new ResourceNotFoundException("Series not found: " + seriesId));
        Series refreshed = seriesCacheService.refreshIfStale(series);
        boolean discarded = seriesFollowRepository.findByUserAndSeries(userUid, seriesId)
                .map(f -> f.getStatus() == FollowStatus.DISCARDED)
                .orElse(false);
        return toView(refreshed, discarded);
    }

    public List<HardcoverSeriesBook> listSeriesBooks(String seriesId) throws ExecutionException, InterruptedException {
        Series series = seriesRepository.findById(seriesId)
                .orElseThrow(() -> new ResourceNotFoundException("Series not found: " + seriesId));
        return hardcoverClient.listSeriesBooks(series.getHardcoverSeriesId());
    }

    private SeriesFollowView toView(Series series, boolean discarded) {
        Series.CachedNextRelease next = series.getCachedNextRelease();
        return new SeriesFollowView(
                series.getId(),
                series.getName(),
                next != null ? next.getTitle() : null,
                next != null && next.getReleaseDate() != null ? LocalDate.parse(next.getReleaseDate()) : null,
                series.getIsCompleted(),
                discarded
        );
    }
}
