package com.booknook.backend.service;

import com.booknook.backend.model.NotificationStage;
import com.booknook.backend.model.Series;
import com.booknook.backend.model.SeriesFollow;
import com.booknook.backend.repository.SeriesFollowRepository;
import com.booknook.backend.repository.SeriesRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

/**
 * Daily job: for every followed series, refresh the cached release date (if stale) and send a
 * push notification at two points — roughly two months before release, and on release day —
 * exactly once each, tracked via {@link SeriesFollow#getLastNotifiedStage()}.
 */
@Component
public class ReleaseCheckJob {

    private static final Logger log = LoggerFactory.getLogger(ReleaseCheckJob.class);

    private final SeriesFollowRepository seriesFollowRepository;
    private final SeriesRepository seriesRepository;
    private final SeriesCacheService seriesCacheService;
    private final NotificationService notificationService;

    public ReleaseCheckJob(SeriesFollowRepository seriesFollowRepository, SeriesRepository seriesRepository,
                            SeriesCacheService seriesCacheService, NotificationService notificationService) {
        this.seriesFollowRepository = seriesFollowRepository;
        this.seriesRepository = seriesRepository;
        this.seriesCacheService = seriesCacheService;
        this.notificationService = notificationService;
    }

    @Scheduled(cron = "0 0 8 * * *")
    public void run() {
        log.info("Running daily series release-check job");
        try {
            List<SeriesFollow> follows = seriesFollowRepository.findAll();
            Map<String, Series> seriesCache = new HashMap<>();

            for (SeriesFollow follow : follows) {
                Series series = seriesCache.computeIfAbsent(follow.getSeriesId(), this::loadAndRefresh);
                if (series == null || series.getCachedNextRelease() == null
                        || series.getCachedNextRelease().getReleaseDate() == null) {
                    continue;
                }
                processFollow(follow, series);
            }
        } catch (ExecutionException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            log.error("Release-check job failed", e);
        }
        log.info("Finished daily series release-check job");
    }

    private Series loadAndRefresh(String seriesId) {
        try {
            Optional<Series> series = seriesRepository.findById(seriesId);
            if (series.isEmpty()) {
                return null;
            }
            return seriesCacheService.refreshIfStale(series.get());
        } catch (ExecutionException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            log.warn("Failed to refresh series {} during release check: {}", seriesId, e.getMessage());
            return null;
        }
    }

    private void processFollow(SeriesFollow follow, Series series) throws ExecutionException, InterruptedException {
        LocalDate releaseDate = series.getCachedNextRelease().getReleaseDate();
        LocalDate today = LocalDate.now();
        String seriesName = series.getName();

        if (releaseDate.isEqual(today) && follow.getLastNotifiedStage() != NotificationStage.RELEASE_DAY) {
            notificationService.notifyUser(follow.getUserUid(), "Out today: " + seriesName,
                    seriesName + " has a new release out today.");
            follow.setLastNotifiedStage(NotificationStage.RELEASE_DAY);
            seriesFollowRepository.save(follow);
        } else if (releaseDate.isEqual(today.plusMonths(2))
                && follow.getLastNotifiedStage() == NotificationStage.NONE) {
            notificationService.notifyUser(follow.getUserUid(), "Coming soon: " + seriesName,
                    seriesName + " releases on " + releaseDate + " — about two months away.");
            follow.setLastNotifiedStage(NotificationStage.TWO_MONTH_WARNING);
            seriesFollowRepository.save(follow);
        }
    }
}
