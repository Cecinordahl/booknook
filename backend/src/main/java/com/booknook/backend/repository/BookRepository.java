package com.booknook.backend.repository;

import com.booknook.backend.dto.BookFilter;
import com.booknook.backend.model.Book;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QuerySnapshot;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Repository
public class BookRepository {

    public static final String COLLECTION = "books";

    private final Firestore firestore;

    public BookRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    private CollectionReference collection() {
        return firestore.collection(COLLECTION);
    }

    public Book save(Book book) throws ExecutionException, InterruptedException {
        CollectionReference collection = collection();
        if (book.getId() == null || book.getId().isBlank()) {
            book.setId(collection.document().getId());
        }
        collection.document(book.getId()).set(book).get();
        return book;
    }

    public Optional<Book> findById(String id) throws ExecutionException, InterruptedException {
        DocumentSnapshot snapshot = collection().document(id).get().get();
        return snapshot.exists() ? Optional.ofNullable(snapshot.toObject(Book.class)) : Optional.empty();
    }

    public void deleteById(String id) throws ExecutionException, InterruptedException {
        collection().document(id).delete().get();
    }

    public List<Book> findByOwner(String ownerUid, BookFilter filter) throws ExecutionException, InterruptedException {
        Query query = collection().whereEqualTo("ownerUid", ownerUid);

        if (filter.getGenre() != null) {
            query = query.whereEqualTo("genre", filter.getGenre());
        }
        if (filter.getStatus() != null) {
            query = query.whereEqualTo("status", filter.getStatus().name());
        }
        if (filter.getFormat() != null) {
            query = query.whereEqualTo("format", filter.getFormat().name());
        }
        if (filter.getMoodTags() != null && !filter.getMoodTags().isEmpty()) {
            query = query.whereArrayContainsAny("moodTags", filter.getMoodTags());
        }

        // Firestore allows only one range-filtered field per query — apply the first one present
        // server-side; any others in the filter are applied in-memory below.
        if (filter.getMinPageCount() != null || filter.getMaxPageCount() != null) {
            query = rangeFilter(query, "pageCount", filter.getMinPageCount(), filter.getMaxPageCount());
        } else if (filter.getMinPublicationYear() != null || filter.getMaxPublicationYear() != null) {
            query = rangeFilter(query, "publicationYear", filter.getMinPublicationYear(), filter.getMaxPublicationYear());
        } else if (filter.getMinRating() != null) {
            query = query.whereGreaterThanOrEqualTo("personalRating", filter.getMinRating());
        }

        query = query.orderBy(filter.getSortBy(),
                filter.isSortDescending() ? Query.Direction.DESCENDING : Query.Direction.ASCENDING);

        QuerySnapshot snapshot = query.get().get();
        List<Book> books = snapshot.getDocuments().stream()
                .map(doc -> doc.toObject(Book.class))
                .collect(Collectors.toList());

        return applyInMemoryFallback(books, filter);
    }

    private Query rangeFilter(Query query, String field, Number min, Number max) {
        if (min != null) {
            query = query.whereGreaterThanOrEqualTo(field, min);
        }
        if (max != null) {
            query = query.whereLessThanOrEqualTo(field, max);
        }
        return query;
    }

    private List<Book> applyInMemoryFallback(List<Book> books, BookFilter filter) {
        return books.stream()
                .filter(b -> filter.getMinPageCount() == null || b.getPageCount() == null
                        || b.getPageCount() >= filter.getMinPageCount())
                .filter(b -> filter.getMaxPageCount() == null || b.getPageCount() == null
                        || b.getPageCount() <= filter.getMaxPageCount())
                .filter(b -> filter.getMinPublicationYear() == null || b.getPublicationYear() == null
                        || b.getPublicationYear() >= filter.getMinPublicationYear())
                .filter(b -> filter.getMaxPublicationYear() == null || b.getPublicationYear() == null
                        || b.getPublicationYear() <= filter.getMaxPublicationYear())
                .filter(b -> filter.getMinRating() == null || b.getPersonalRating() == null
                        || b.getPersonalRating() >= filter.getMinRating())
                .collect(Collectors.toList());
    }

    public List<Book> findBySeriesId(String ownerUid, String seriesId) throws ExecutionException, InterruptedException {
        QuerySnapshot snapshot = collection()
                .whereEqualTo("ownerUid", ownerUid)
                .whereEqualTo("seriesId", seriesId)
                .get().get();
        return snapshot.getDocuments().stream().map(doc -> doc.toObject(Book.class)).collect(Collectors.toList());
    }

    /** Distinct, sorted genre strings the owner has already used — powers the Add Book genre autocomplete. */
    public List<String> findDistinctGenresByOwner(String ownerUid) throws ExecutionException, InterruptedException {
        QuerySnapshot snapshot = collection().whereEqualTo("ownerUid", ownerUid).get().get();
        return snapshot.getDocuments().stream()
                .map(doc -> doc.getString("genre"))
                .filter(genre -> genre != null && !genre.isBlank())
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toList());
    }

    public void deleteAllForUser(String ownerUid) throws ExecutionException, InterruptedException {
        QuerySnapshot snapshot = collection().whereEqualTo("ownerUid", ownerUid).get().get();
        for (var doc : snapshot.getDocuments()) {
            doc.getReference().delete().get();
        }
    }
}
