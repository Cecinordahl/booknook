package com.booknook.backend.repository;

import com.booknook.backend.model.PushSubscription;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import org.springframework.stereotype.Repository;

import java.util.Base64;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Repository
public class PushSubscriptionRepository {

    public static final String COLLECTION = "pushSubscriptions";

    private final Firestore firestore;

    public PushSubscriptionRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    private CollectionReference collection() {
        return firestore.collection(COLLECTION);
    }

    public PushSubscription save(PushSubscription subscription) throws ExecutionException, InterruptedException {
        // Deterministic ID from the endpoint so re-subscribing (e.g. after a browser refresh)
        // upserts instead of creating duplicate rows.
        String id = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(subscription.getEndpoint().getBytes());
        subscription.setId(id);
        collection().document(id).set(subscription).get();
        return subscription;
    }

    public void deleteByEndpoint(String endpoint) throws ExecutionException, InterruptedException {
        String id = Base64.getUrlEncoder().withoutPadding().encodeToString(endpoint.getBytes());
        collection().document(id).delete().get();
    }

    public List<PushSubscription> findByUser(String userUid) throws ExecutionException, InterruptedException {
        QuerySnapshot snapshot = collection().whereEqualTo("userUid", userUid).get().get();
        return snapshot.getDocuments().stream().map(doc -> doc.toObject(PushSubscription.class)).collect(Collectors.toList());
    }

    public void deleteAllForUser(String userUid) throws ExecutionException, InterruptedException {
        QuerySnapshot snapshot = collection().whereEqualTo("userUid", userUid).get().get();
        for (var doc : snapshot.getDocuments()) {
            doc.getReference().delete().get();
        }
    }
}
