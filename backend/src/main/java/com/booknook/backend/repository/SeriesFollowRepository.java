package com.booknook.backend.repository;

import com.booknook.backend.model.SeriesFollow;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Repository
public class SeriesFollowRepository {

    public static final String COLLECTION = "seriesFollows";

    private final Firestore firestore;

    public SeriesFollowRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    private CollectionReference collection() {
        return firestore.collection(COLLECTION);
    }

    public SeriesFollow save(SeriesFollow follow) throws ExecutionException, InterruptedException {
        if (follow.getId() == null || follow.getId().isBlank()) {
            follow.setId(SeriesFollow.buildId(follow.getUserUid(), follow.getSeriesId()));
        }
        collection().document(follow.getId()).set(follow).get();
        return follow;
    }

    public void deleteByUserAndSeries(String userUid, String seriesId) throws ExecutionException, InterruptedException {
        collection().document(SeriesFollow.buildId(userUid, seriesId)).delete().get();
    }

    public List<SeriesFollow> findByUser(String userUid) throws ExecutionException, InterruptedException {
        QuerySnapshot snapshot = collection().whereEqualTo("userUid", userUid).get().get();
        return snapshot.getDocuments().stream().map(doc -> doc.toObject(SeriesFollow.class)).collect(Collectors.toList());
    }

    public List<SeriesFollow> findAll() throws ExecutionException, InterruptedException {
        QuerySnapshot snapshot = collection().get().get();
        return snapshot.getDocuments().stream().map(doc -> doc.toObject(SeriesFollow.class)).collect(Collectors.toList());
    }

    public Optional<SeriesFollow> findByUserAndSeries(String userUid, String seriesId) throws ExecutionException, InterruptedException {
        DocumentSnapshot snapshot = collection().document(SeriesFollow.buildId(userUid, seriesId)).get().get();
        return snapshot.exists() ? Optional.ofNullable(snapshot.toObject(SeriesFollow.class)) : Optional.empty();
    }

    public void deleteAllForUser(String userUid) throws ExecutionException, InterruptedException {
        QuerySnapshot snapshot = collection().whereEqualTo("userUid", userUid).get().get();
        for (var doc : snapshot.getDocuments()) {
            doc.getReference().delete().get();
        }
    }
}
