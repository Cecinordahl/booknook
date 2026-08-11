package com.booknook.backend.dto;

import java.time.LocalDate;

/**
 * Everything the release-check flow needs from a single Hardcover series lookup: the next
 * (if any) upcoming release, and whether Hardcover has the series itself marked completed —
 * lets the UI distinguish "finished series, nothing more coming" from "ongoing series, next
 * book just isn't dated yet."
 */
public record HardcoverSeriesStatus(String nextReleaseTitle, LocalDate nextReleaseDate, boolean isCompleted) {
}
