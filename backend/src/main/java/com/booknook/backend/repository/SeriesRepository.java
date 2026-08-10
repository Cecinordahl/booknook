package com.booknook.backend.repository;

import com.booknook.backend.model.Series;
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
public class SeriesRepository {

    public static final String COLLECTION = "series";

    private final Firestore firestore;

    public SeriesRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    private CollectionReference collection() {
        return firestore.collection(COLLECTION);
    }

    public Series save(Series series) throws ExecutionException, InterruptedException {
        if (series.getId() == null || series.getId().isBlank()) {
            series.setId(collection().document().getId());
        }
        collection().document(series.getId()).set(series).get();
        return series;
    }

    public Optional<Series> findById(String id) throws ExecutionException, InterruptedException {
        DocumentSnapshot snapshot = collection().document(id).get().get();
        return snapshot.exists() ? Optional.ofNullable(snapshot.toObject(Series.class)) : Optional.empty();
    }

    public Optional<Series> findByHardcoverSeriesId(String hardcoverSeriesId) throws ExecutionException, InterruptedException {
        QuerySnapshot snapshot = collection()
                .whereEqualTo("hardcoverSeriesId", hardcoverSeriesId)
                .limit(1)
                .get().get();
        return snapshot.getDocuments().stream()
                .findFirst()
                .map(doc -> doc.toObject(Series.class));
    }

    public List<Series> findAll() throws ExecutionException, InterruptedException {
        QuerySnapshot snapshot = collection().get().get();
        return snapshot.getDocuments().stream().map(doc -> doc.toObject(Series.class)).collect(Collectors.toList());
    }
}
