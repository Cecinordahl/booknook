package com.booknook.backend.controller;

import com.booknook.backend.security.FirebaseAuthenticatedUser;
import com.booknook.backend.service.AccountDeletionService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/me")
public class AccountController {

    private final AccountDeletionService accountDeletionService;

    public AccountController(AccountDeletionService accountDeletionService) {
        this.accountDeletionService = accountDeletionService;
    }

    /** GDPR right-to-erasure: deletes all of the caller's data and revokes their access. */
    @DeleteMapping
    public void deleteAccount(@AuthenticationPrincipal FirebaseAuthenticatedUser principal)
            throws ExecutionException, InterruptedException {
        accountDeletionService.deleteAccount(principal.uid(), principal.email());
    }
}
