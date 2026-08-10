package com.booknook.backend.service;

import com.booknook.backend.repository.AllowlistRepository;
import com.booknook.backend.repository.BookRepository;
import com.booknook.backend.repository.PushSubscriptionRepository;
import com.booknook.backend.repository.SeriesFollowRepository;
import com.booknook.backend.repository.UserAccountRepository;
import com.booknook.backend.security.AllowlistService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

/**
 * GDPR right-to-erasure: deletes everything tied to a user's account. Their allowlist entry is
 * removed too — re-inviting them later requires the owner to add it back, which is the intended
 * behavior (deletion should actually revoke access, not just clear data).
 */
@Service
public class AccountDeletionService {

    private static final Logger log = LoggerFactory.getLogger(AccountDeletionService.class);

    private final BookRepository bookRepository;
    private final SeriesFollowRepository seriesFollowRepository;
    private final PushSubscriptionRepository pushSubscriptionRepository;
    private final UserAccountRepository userAccountRepository;
    private final AllowlistRepository allowlistRepository;

    public AccountDeletionService(BookRepository bookRepository, SeriesFollowRepository seriesFollowRepository,
                                   PushSubscriptionRepository pushSubscriptionRepository,
                                   UserAccountRepository userAccountRepository,
                                   AllowlistRepository allowlistRepository) {
        this.bookRepository = bookRepository;
        this.seriesFollowRepository = seriesFollowRepository;
        this.pushSubscriptionRepository = pushSubscriptionRepository;
        this.userAccountRepository = userAccountRepository;
        this.allowlistRepository = allowlistRepository;
    }

    public void deleteAccount(String uid, String email) throws ExecutionException, InterruptedException {
        bookRepository.deleteAllForUser(uid);
        seriesFollowRepository.deleteAllForUser(uid);
        pushSubscriptionRepository.deleteAllForUser(uid);
        userAccountRepository.deleteByUid(uid);
        allowlistRepository.remove(AllowlistService.normalize(email));

        try {
            // Resolved at call time rather than injected: FirebaseAuth is a final class Spring
            // can't lazily proxy, and this service must not force Firebase init at app startup.
            FirebaseAuth.getInstance().deleteUser(uid);
        } catch (FirebaseAuthException e) {
            log.error("Deleted Firestore data for {} but failed to delete the Firebase Auth user record", uid, e);
        }
    }
}
