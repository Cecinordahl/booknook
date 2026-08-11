package com.booknook.backend.controller;

import com.booknook.backend.dto.UpdateNotificationPreferencesRequest;
import com.booknook.backend.model.UserAccount;
import com.booknook.backend.security.FirebaseAuthenticatedUser;
import com.booknook.backend.service.UserAccountService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/me")
public class AuthController {

    private final UserAccountService userAccountService;

    public AuthController(UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }

    @GetMapping
    public UserAccount me(@AuthenticationPrincipal FirebaseAuthenticatedUser principal)
            throws ExecutionException, InterruptedException {
        return userAccountService.getOrProvision(principal);
    }

    @PutMapping("/notification-preferences")
    public UserAccount updateNotificationPreferences(@AuthenticationPrincipal FirebaseAuthenticatedUser principal,
                                                       @RequestBody UpdateNotificationPreferencesRequest request)
            throws ExecutionException, InterruptedException {
        return userAccountService.updateNotificationIntervals(principal.uid(), request.intervalDays());
    }
}
