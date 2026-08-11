package com.booknook.backend.controller;

import com.booknook.backend.dto.HardcoverSeriesBook;
import com.booknook.backend.dto.SeriesFollowView;
import com.booknook.backend.security.FirebaseAuthenticatedUser;
import com.booknook.backend.service.SeriesService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Series are attached to a user's library automatically when a book with a resolved series is
 * added (see {@code BookService#create}) — there's no manual "search for a series" endpoint
 * anymore, since Hardcover's series search index is too noisy (books showing up as series
 * results, heavy duplication) to expose directly.
 */
@RestController
@RequestMapping("/api/series")
public class SeriesController {

    private final SeriesService seriesService;

    public SeriesController(SeriesService seriesService) {
        this.seriesService = seriesService;
    }

    @GetMapping("/followed")
    public List<SeriesFollowView> followed(@AuthenticationPrincipal FirebaseAuthenticatedUser principal)
            throws ExecutionException, InterruptedException {
        return seriesService.listFollowed(principal.uid());
    }

    @GetMapping("/{seriesId}")
    public SeriesFollowView get(@AuthenticationPrincipal FirebaseAuthenticatedUser principal, @PathVariable String seriesId)
            throws ExecutionException, InterruptedException {
        return seriesService.getSeriesDetail(principal.uid(), seriesId);
    }

    @GetMapping("/{seriesId}/books")
    public List<HardcoverSeriesBook> books(@PathVariable String seriesId) throws ExecutionException, InterruptedException {
        return seriesService.listSeriesBooks(seriesId);
    }

    @DeleteMapping("/{seriesId}/follow")
    public void unfollow(@AuthenticationPrincipal FirebaseAuthenticatedUser principal, @PathVariable String seriesId)
            throws ExecutionException, InterruptedException {
        seriesService.unfollow(principal.uid(), seriesId);
    }

    @PostMapping("/{seriesId}/reactivate")
    public void reactivate(@AuthenticationPrincipal FirebaseAuthenticatedUser principal, @PathVariable String seriesId)
            throws ExecutionException, InterruptedException {
        seriesService.reactivate(principal.uid(), seriesId);
    }
}
