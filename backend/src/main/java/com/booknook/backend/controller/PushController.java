package com.booknook.backend.controller;

import com.booknook.backend.dto.PushSubscribeRequest;
import com.booknook.backend.security.FirebaseAuthenticatedUser;
import com.booknook.backend.service.PushSubscriptionService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/push")
public class PushController {

    private final PushSubscriptionService pushSubscriptionService;

    public PushController(PushSubscriptionService pushSubscriptionService) {
        this.pushSubscriptionService = pushSubscriptionService;
    }

    @PostMapping("/subscribe")
    public void subscribe(@AuthenticationPrincipal FirebaseAuthenticatedUser principal,
                           @RequestBody PushSubscribeRequest request) throws ExecutionException, InterruptedException {
        pushSubscriptionService.subscribe(principal.uid(), request);
    }

    @PostMapping("/unsubscribe")
    public void unsubscribe(@RequestBody Map<String, String> body) throws ExecutionException, InterruptedException {
        pushSubscriptionService.unsubscribe(body.get("endpoint"));
    }
}
