package com.booknook.backend.service;

import com.booknook.backend.dto.HardcoverSeriesMatch;
import com.booknook.backend.dto.SeriesFollowView;
import com.booknook.backend.model.Series;
import com.booknook.backend.model.SeriesFollow;
import com.booknook.backend.repository.SeriesFollowRepository;
import com.booknook.backend.repository.SeriesRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

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

    public List<HardcoverSeriesMatch> search(String query) {
        return hardcoverClient.searchSeries(query);
    }

    public SeriesFollow follow(String userUid, String hardcoverSeriesId, String seriesName)
            throws ExecutionException, InterruptedException {
        Series series = seriesRepository.findByHardcoverSeriesId(hardcoverSeriesId)
                .orElseGet(() -> {
                    Series s = new Series();
                    s.setHardcoverSeriesId(hardcoverSeriesId);
                    s.setName(seriesName);
                    try {
                        return seriesRepository.save(s);
                    } catch (ExecutionException | InterruptedException e) {
                        throw new RuntimeException("Failed to create series record", e);
                    }
                });

        SeriesFollow follow = new SeriesFollow();
        follow.setUserUid(userUid);
        follow.setSeriesId(series.getId());
        follow.setFollowedAt(Instant.now());
        return seriesFollowRepository.save(follow);
    }

    public void unfollow(String userUid, String seriesId) throws ExecutionException, InterruptedException {
        seriesFollowRepository.deleteByUserAndSeries(userUid, seriesId);
    }

    public List<SeriesFollowView> listFollowedWithNextRelease(String userUid) throws ExecutionException, InterruptedException {
        List<SeriesFollow> follows = seriesFollowRepository.findByUser(userUid);
        List<SeriesFollowView> views = new java.util.ArrayList<>();

        for (SeriesFollow follow : follows) {
            seriesRepository.findById(follow.getSeriesId()).ifPresent(series -> {
                try {
                    Series refreshed = seriesCacheService.refreshIfStale(series);
                    Series.CachedNextRelease next = refreshed.getCachedNextRelease();
                    views.add(new SeriesFollowView(
                            refreshed.getId(),
                            refreshed.getName(),
                            next != null ? next.getTitle() : null,
                            next != null ? next.getReleaseDate() : null
                    ));
                } catch (ExecutionException | InterruptedException e) {
                    if (e instanceof InterruptedException) {
                        Thread.currentThread().interrupt();
                    }
                    throw new RuntimeException("Failed to refresh series cache for " + series.getId(), e);
                }
            });
        }

        return views;
    }
}
