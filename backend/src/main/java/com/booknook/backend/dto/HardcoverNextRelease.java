package com.booknook.backend.dto;

import java.time.LocalDate;

public record HardcoverNextRelease(String title, LocalDate releaseDate) {
}
