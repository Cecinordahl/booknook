package com.booknook.backend.dto;

import java.time.LocalDate;

public record HardcoverSeriesBook(String title, String coverImageUrl, LocalDate releaseDate, Double position) {
}
