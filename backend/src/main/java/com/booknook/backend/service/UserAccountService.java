package com.booknook.backend.service;

import com.booknook.backend.model.UserAccount;
import com.booknook.backend.repository.UserAccountRepository;
import com.booknook.backend.security.FirebaseAuthenticatedUser;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.ExecutionException;

@Service
public class UserAccountService {

    private final UserAccountRepository userAccountRepository;

    public UserAccountService(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    /** Creates the Firestore user profile on a user's first authenticated request, if needed. */
    public UserAccount getOrProvision(FirebaseAuthenticatedUser principal) throws ExecutionException, InterruptedException {
        return userAccountRepository.findByUid(principal.uid())
                .orElseGet(() -> {
                    UserAccount account = new UserAccount(principal.uid(), principal.email(),
                            principal.displayName(), Instant.now());
                    try {
                        return userAccountRepository.save(account);
                    } catch (ExecutionException | InterruptedException e) {
                        throw new RuntimeException("Failed to provision user account for " + principal.uid(), e);
                    }
                });
    }
}
