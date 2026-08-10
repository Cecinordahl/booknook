package com.booknook.backend.service;

import com.booknook.backend.config.BooknookProperties;
import com.booknook.backend.model.PushSubscription;
import com.booknook.backend.repository.PushSubscriptionRepository;
import jakarta.annotation.PostConstruct;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.security.GeneralSecurityException;
import java.security.Security;
import java.util.List;
import java.util.concurrent.ExecutionException;

/** Sends Web Push notifications (release reminders) to a user's subscribed browsers/devices. */
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final PushSubscriptionRepository pushSubscriptionRepository;
    private final BooknookProperties properties;
    private PushService pushService;

    public NotificationService(PushSubscriptionRepository pushSubscriptionRepository, BooknookProperties properties) {
        this.pushSubscriptionRepository = pushSubscriptionRepository;
        this.properties = properties;
    }

    @PostConstruct
    public void init() {
        Security.addProvider(new BouncyCastleProvider());

        String publicKey = properties.getPush().getVapidPublicKey();
        String privateKey = properties.getPush().getVapidPrivateKey();
        if (publicKey == null || publicKey.isBlank() || privateKey == null || privateKey.isBlank()) {
            log.warn("VAPID_PUBLIC_KEY/VAPID_PRIVATE_KEY not set — push notifications are disabled "
                    + "until configured. See README for how to generate a VAPID key pair.");
            return;
        }

        try {
            this.pushService = new PushService(publicKey, privateKey, properties.getPush().getVapidSubject());
        } catch (GeneralSecurityException e) {
            log.error("Failed to initialize Web Push service with the configured VAPID keys", e);
        }
    }

    public void notifyUser(String userUid, String title, String body) {
        if (pushService == null) {
            log.warn("Push not configured — skipping notification '{}' for user {}", title, userUid);
            return;
        }

        List<PushSubscription> subscriptions;
        try {
            subscriptions = pushSubscriptionRepository.findByUser(userUid);
        } catch (ExecutionException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            log.error("Failed to load push subscriptions for user {}", userUid, e);
            return;
        }

        String payload = """
                {"title":"%s","body":"%s"}""".formatted(escape(title), escape(body));

        for (PushSubscription sub : subscriptions) {
            try {
                var subscription = new nl.martijndwars.webpush.Subscription(
                        sub.getEndpoint(),
                        new nl.martijndwars.webpush.Subscription.Keys(sub.getKeys().getP256dh(), sub.getKeys().getAuth())
                );
                pushService.send(new Notification(subscription, payload));
            } catch (Exception e) {
                log.warn("Failed to deliver push notification to subscription {}: {}", sub.getId(), e.getMessage());
            }
        }
    }

    private String escape(String s) {
        return s == null ? "" : s.replace("\"", "'");
    }
}
