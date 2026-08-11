package com.booknook.backend.service;

import com.booknook.backend.exception.ValidationException;
import com.booknook.backend.model.UserAccount;
import com.booknook.backend.repository.UserAccountRepository;
import com.booknook.backend.security.FirebaseAuthenticatedUser;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class UserAccountService {

    /** 2 months before + release day — matches the app's original hardcoded behavior. */
    private static final List<Integer> DEFAULT_NOTIFICATION_INTERVAL_DAYS = List.of(60, 0);
    private static final int MAX_NOTIFICATION_INTERVALS = 3;

    private final UserAccountRepository userAccountRepository;

    public UserAccountService(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    /** Creates the Firestore user profile on a user's first authenticated request, if needed. */
    public UserAccount getOrProvision(FirebaseAuthenticatedUser principal) throws ExecutionException, InterruptedException {
        UserAccount account = userAccountRepository.findByUid(principal.uid())
                .orElseGet(() -> {
                    UserAccount created = new UserAccount(principal.uid(), principal.email(),
                            principal.displayName(), Instant.now());
                    try {
                        return userAccountRepository.save(created);
                    } catch (ExecutionException | InterruptedException e) {
                        throw new RuntimeException("Failed to provision user account for " + principal.uid(), e);
                    }
                });

        if (account.getNotificationIntervalDays() == null) {
            // Read-time default only — not persisted until the user explicitly saves their own
            // choice, so this doesn't require a migration for existing accounts.
            account.setNotificationIntervalDays(DEFAULT_NOTIFICATION_INTERVAL_DAYS);
        }
        return account;
    }

    public UserAccount updateNotificationIntervals(String uid, List<Integer> intervalDays)
            throws ExecutionException, InterruptedException {
        if (intervalDays.size() > MAX_NOTIFICATION_INTERVALS) {
            throw new ValidationException("At most " + MAX_NOTIFICATION_INTERVALS + " notification intervals are allowed.");
        }
        if (intervalDays.stream().anyMatch(d -> d == null || d < 0)) {
            throw new ValidationException("Notification intervals must be non-negative day counts.");
        }

        UserAccount account = userAccountRepository.findByUid(uid)
                .orElseThrow(() -> new ValidationException("No account found for " + uid));
        account.setNotificationIntervalDays(intervalDays);
        return userAccountRepository.save(account);
    }
}
