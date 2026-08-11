package com.booknook.backend.dto;

import java.time.LocalDate;

public record SeriesFollowView(String seriesId, String seriesName, String nextReleaseTitle,
                                LocalDate nextReleaseDate, Boolean isCompleted, boolean discarded) {
}
