package com.booknook.backend.service;

import com.booknook.backend.model.FollowStatus;
import com.booknook.backend.model.Series;
import com.booknook.backend.model.SeriesFollow;
import com.booknook.backend.model.UserAccount;
import com.booknook.backend.repository.SeriesFollowRepository;
import com.booknook.backend.repository.SeriesRepository;
import com.booknook.backend.repository.UserAccountRepository;
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
 * push notification at each of the following user's configured "days before release" intervals
 * (see {@link UserAccount#getNotificationIntervalDays()}) — exactly once per interval, tracked
 * via {@link SeriesFollow#getNotifiedIntervalDays()}.
 */
@Component
public class ReleaseCheckJob {

    private static final Logger log = LoggerFactory.getLogger(ReleaseCheckJob.class);

    private final SeriesFollowRepository seriesFollowRepository;
    private final SeriesRepository seriesRepository;
    private final SeriesCacheService seriesCacheService;
    private final NotificationService notificationService;
    private final UserAccountRepository userAccountRepository;

    public ReleaseCheckJob(SeriesFollowRepository seriesFollowRepository, SeriesRepository seriesRepository,
                            SeriesCacheService seriesCacheService, NotificationService notificationService,
                            UserAccountRepository userAccountRepository) {
        this.seriesFollowRepository = seriesFollowRepository;
        this.seriesRepository = seriesRepository;
        this.seriesCacheService = seriesCacheService;
        this.notificationService = notificationService;
        this.userAccountRepository = userAccountRepository;
    }

    @Scheduled(cron = "0 0 8 * * *")
    public void run() {
        log.info("Running daily series release-check job");
        try {
            List<SeriesFollow> follows = seriesFollowRepository.findAll();
            Map<String, Series> seriesCache = new HashMap<>();
            Map<String, List<Integer>> intervalsByUser = new HashMap<>();

            for (SeriesFollow follow : follows) {
                if (follow.getStatus() != FollowStatus.ACTIVE) {
                    continue;
                }
                Series series = seriesCache.computeIfAbsent(follow.getSeriesId(), this::loadAndRefresh);
                if (series == null || series.getCachedNextRelease() == null
                        || series.getCachedNextRelease().getReleaseDate() == null) {
                    continue;
                }
                List<Integer> intervalDays = intervalsByUser.computeIfAbsent(follow.getUserUid(), this::loadIntervalDays);
                processFollow(follow, series, intervalDays);
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

    private List<Integer> loadIntervalDays(String userUid) {
        try {
            return userAccountRepository.findByUid(userUid)
                    .map(UserAccount::getNotificationIntervalDays)
                    .filter(days -> days != null && !days.isEmpty())
                    .orElse(List.of(60, 0));
        } catch (ExecutionException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            log.warn("Failed to load notification preferences for {}, using default: {}", userUid, e.getMessage());
            return List.of(60, 0);
        }
    }

    private void processFollow(SeriesFollow follow, Series series, List<Integer> intervalDays)
            throws ExecutionException, InterruptedException {
        LocalDate releaseDate = LocalDate.parse(series.getCachedNextRelease().getReleaseDate());
        LocalDate today = LocalDate.now();
        String seriesName = series.getName();
        boolean changed = false;

        for (int days : intervalDays) {
            if (!releaseDate.minusDays(days).isEqual(today) || follow.getNotifiedIntervalDays().contains(days)) {
                continue;
            }
            if (days == 0) {
                notificationService.notifyUser(follow.getUserUid(), "Out today: " + seriesName,
                        seriesName + " has a new release out today.");
            } else {
                notificationService.notifyUser(follow.getUserUid(), "Coming soon: " + seriesName,
                        seriesName + " releases on " + releaseDate + " — " + days + " days away.");
            }
            follow.getNotifiedIntervalDays().add(days);
            changed = true;
        }

        if (changed) {
            seriesFollowRepository.save(follow);
        }
    }
}
