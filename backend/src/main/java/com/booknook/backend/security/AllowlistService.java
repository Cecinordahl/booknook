package com.booknook.backend.security;

import com.booknook.backend.repository.AllowlistRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.concurrent.ExecutionException;

/**
 * Checks a Firebase-authenticated email against the Firestore invite list.
 *
 * <p>For v1 there is no admin UI to manage the allowlist — entries are added/removed directly in
 * the Firestore console. See README for the exact steps. This keeps the initial build small; a
 * self-service admin screen is a reasonable follow-up once the app has more than a handful of
 * users.
 */
@Service
public class AllowlistService {

    private static final Logger log = LoggerFactory.getLogger(AllowlistService.class);

    private final AllowlistRepository allowlistRepository;

    public AllowlistService(AllowlistRepository allowlistRepository) {
        this.allowlistRepository = allowlistRepository;
    }

    public boolean isAllowed(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        try {
            return allowlistRepository.exists(normalize(email));
        } catch (ExecutionException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            log.error("Failed to check allowlist for email lookup", e);
            return false;
        }
    }

    public static String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
