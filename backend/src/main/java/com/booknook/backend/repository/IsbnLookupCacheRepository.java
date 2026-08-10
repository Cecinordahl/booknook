package com.booknook.backend.repository;

import com.booknook.backend.model.IsbnLookupCache;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Repository
public class IsbnLookupCacheRepository {

    public static final String COLLECTION = "isbnLookupCache";

    private final Firestore firestore;

    public IsbnLookupCacheRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    private CollectionReference collection() {
        return firestore.collection(COLLECTION);
    }

    public Optional<IsbnLookupCache> findByIsbn(String isbn) throws ExecutionException, InterruptedException {
        DocumentSnapshot snapshot = collection().document(isbn).get().get();
        return snapshot.exists() ? Optional.ofNullable(snapshot.toObject(IsbnLookupCache.class)) : Optional.empty();
    }

    public void save(IsbnLookupCache cache) throws ExecutionException, InterruptedException {
        collection().document(cache.getIsbn()).set(cache).get();
    }
}
