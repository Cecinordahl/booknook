package com.booknook.backend.controller;

import com.booknook.backend.dto.FollowSeriesRequest;
import com.booknook.backend.dto.HardcoverSeriesMatch;
import com.booknook.backend.dto.SeriesFollowView;
import com.booknook.backend.security.FirebaseAuthenticatedUser;
import com.booknook.backend.service.SeriesService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/series")
public class SeriesController {

    private final SeriesService seriesService;

    public SeriesController(SeriesService seriesService) {
        this.seriesService = seriesService;
    }

    @GetMapping("/search")
    public List<HardcoverSeriesMatch> search(@RequestParam String q) {
        return seriesService.search(q);
    }

    @PostMapping("/follow")
    public void follow(@AuthenticationPrincipal FirebaseAuthenticatedUser principal,
                        @RequestBody FollowSeriesRequest request) throws ExecutionException, InterruptedException {
        seriesService.follow(principal.uid(), request.hardcoverSeriesId(), request.seriesName());
    }

    @DeleteMapping("/{seriesId}/follow")
    public void unfollow(@AuthenticationPrincipal FirebaseAuthenticatedUser principal, @PathVariable String seriesId)
            throws ExecutionException, InterruptedException {
        seriesService.unfollow(principal.uid(), seriesId);
    }

    @GetMapping("/followed")
    public List<SeriesFollowView> followed(@AuthenticationPrincipal FirebaseAuthenticatedUser principal)
            throws ExecutionException, InterruptedException {
        return seriesService.listFollowedWithNextRelease(principal.uid());
    }
}
