package com.booknook.backend.model;

/** Tracks which release-reminder a follow has already received, so the daily job never repeats one. */
public enum NotificationStage {
    NONE,
    TWO_MONTH_WARNING,
    RELEASE_DAY
}
