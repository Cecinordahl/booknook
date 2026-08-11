package com.booknook.backend.dto;

/** The series a specific book (resolved by ISBN) belongs to, per Hardcover's "featured" series link. */
public record HardcoverBookSeriesMatch(String hardcoverSeriesId, String seriesName, Double position) {
}
