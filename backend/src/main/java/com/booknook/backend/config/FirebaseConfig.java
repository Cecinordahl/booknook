package com.booknook.backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * Initializes the Firebase Admin SDK from a service-account JSON file (see README for how to
 * generate one). Locally this points at a file on disk; on Render the same JSON is typically
 * written to a path from a "secret file" mount, referenced by FIREBASE_CREDENTIALS_PATH.
 *
 * <p>FIREBASE_CREDENTIALS_PATH (and FIREBASE_PROJECT_ID) are required for the app to boot at all
 * — Firebase backs both auth and every data read/write, so there's no meaningful degraded mode
 * without it. Other env vars (HARDCOVER_API_KEY, VAPID keys) are optional at boot; only the
 * features that need them fail until configured.
 */
@Configuration
@EnableConfigurationProperties(BooknookProperties.class)
public class FirebaseConfig {

    private static final Logger log = LoggerFactory.getLogger(FirebaseConfig.class);

    @Value("${booknook.firebase.credentials-path}")
    private String credentialsPath;

    @PostConstruct
    public void initFirebase() throws IOException {
        if (!FirebaseApp.getApps().isEmpty()) {
            return;
        }
        if (credentialsPath == null || credentialsPath.isBlank()) {
            throw new IllegalStateException(
                    "FIREBASE_CREDENTIALS_PATH is not set. Booknook's backend cannot start without "
                            + "it — see the README's Firebase setup section.");
        }
        try (FileInputStream serviceAccount = new FileInputStream(credentialsPath)) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();
            FirebaseApp.initializeApp(options);
            log.info("Firebase Admin SDK initialized");
        }
    }

    @Bean
    public Firestore firestore() {
        return FirestoreClient.getFirestore();
    }
}
