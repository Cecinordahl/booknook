package com.booknook.backend.service;

import com.booknook.backend.dto.PushSubscribeRequest;
import com.booknook.backend.model.PushSubscription;
import com.booknook.backend.repository.PushSubscriptionRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.ExecutionException;

@Service
public class PushSubscriptionService {

    private final PushSubscriptionRepository pushSubscriptionRepository;

    public PushSubscriptionService(PushSubscriptionRepository pushSubscriptionRepository) {
        this.pushSubscriptionRepository = pushSubscriptionRepository;
    }

    public void subscribe(String userUid, PushSubscribeRequest request) throws ExecutionException, InterruptedException {
        PushSubscription subscription = new PushSubscription();
        subscription.setUserUid(userUid);
        subscription.setEndpoint(request.endpoint());
        subscription.setKeys(new PushSubscription.Keys(request.keys().p256dh(), request.keys().auth()));
        subscription.setCreatedAt(Instant.now());
        pushSubscriptionRepository.save(subscription);
    }

    public void unsubscribe(String endpoint) throws ExecutionException, InterruptedException {
        pushSubscriptionRepository.deleteByEndpoint(endpoint);
    }
}
