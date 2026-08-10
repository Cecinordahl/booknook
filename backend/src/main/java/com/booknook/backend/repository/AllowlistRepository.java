package com.booknook.backend.repository;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.booknook.backend.model.AllowlistEntry;
import org.springframework.stereotype.Repository;

import java.util.concurrent.ExecutionException;

@Repository
public class AllowlistRepository {

    public static final String COLLECTION = "allowlist";

    private final Firestore firestore;

    public AllowlistRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    public boolean exists(String normalizedEmail) throws ExecutionException, InterruptedException {
        DocumentSnapshot snapshot = firestore.collection(COLLECTION)
                .document(normalizedEmail)
                .get()
                .get();
        return snapshot.exists();
    }

    public void put(AllowlistEntry entry) throws ExecutionException, InterruptedException {
        firestore.collection(COLLECTION)
                .document(entry.getEmail())
                .set(entry)
                .get();
    }

    public void remove(String normalizedEmail) throws ExecutionException, InterruptedException {
        firestore.collection(COLLECTION)
                .document(normalizedEmail)
                .delete()
                .get();
    }
}
