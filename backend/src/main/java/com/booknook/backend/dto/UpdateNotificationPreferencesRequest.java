package com.booknook.backend.dto;

import java.util.List;

public record UpdateNotificationPreferencesRequest(List<Integer> intervalDays) {
}
