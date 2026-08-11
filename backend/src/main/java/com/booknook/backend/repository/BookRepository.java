package com.booknook.backend.repository;

import com.booknook.backend.dto.BookFilter;
import com.booknook.backend.model.Book;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteBatch;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
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

    /**
     * Writes many books in Firestore batches (500 ops max per batch — the Firestore limit) instead
     * of one round-trip per document. Used by the Goodreads import, where a library can run into
     * the hundreds of books and per-document writes would make the request unreasonably slow.
     */
    public List<Book> saveAll(List<Book> books) throws ExecutionException, InterruptedException {
        CollectionReference collection = collection();
        List<Book> saved = new ArrayList<>();
        for (int start = 0; start < books.size(); start += 500) {
            List<Book> chunk = books.subList(start, Math.min(start + 500, books.size()));
            WriteBatch batch = firestore.batch();
            for (Book book : chunk) {
                if (book.getId() == null || book.getId().isBlank()) {
                    book.setId(collection.document().getId());
                }
                batch.set(collection.document(book.getId()), book);
            }
            batch.commit().get();
            saved.addAll(chunk);
        }
        return saved;
    }

    public Optional<Book> findById(String id) throws ExecutionException, InterruptedException {
        DocumentSnapshot snapshot = collection().document(id).get().get();
        return snapshot.exists() ? Optional.ofNullable(snapshot.toObject(Book.class)) : Optional.empty();
    }

    public void deleteById(String id) throws ExecutionException, InterruptedException {
        collection().document(id).delete().get();
    }

    /**
     * Filters and sorts entirely in memory after a single ownerUid-only fetch, rather than
     * pushing filter/orderBy combinations into the Firestore query. Firestore requires a
     * purpose-built composite index for every distinct combination of equality/range/orderBy
     * fields used together — with 6+ filterable fields and 5 sortable ones here, that's a
     * combinatorial number of indexes to predict and pre-create, and an un-provisioned one fails
     * the whole request (a real bug hit in practice, not theoretical). At this app's scale (one
     * person's library, realistically low hundreds of books) fetching by owner and filtering in
     * Java is simpler and needs zero composite indexes — Firestore always has the single-field
     * ownerUid index automatically.
     */
    public List<Book> findByOwner(String ownerUid, BookFilter filter) throws ExecutionException, InterruptedException {
        QuerySnapshot snapshot = collection().whereEqualTo("ownerUid", ownerUid).get().get();
        List<Book> books = snapshot.getDocuments().stream()
                .map(doc -> doc.toObject(Book.class))
                .collect(Collectors.toList());

        List<Book> filtered = applyFilters(books, filter);
        filtered.sort(comparatorFor(filter));
        return filtered;
    }

    private List<Book> applyFilters(List<Book> books, BookFilter filter) {
        return books.stream()
                .filter(b -> filter.getGenre() == null || filter.getGenre().equalsIgnoreCase(b.getGenre()))
                .filter(b -> filter.getSource() == null || filter.getSource().equalsIgnoreCase(b.getSource()))
                .filter(b -> filter.getStatus() == null || filter.getStatus() == b.getStatus())
                .filter(b -> filter.getFormat() == null || filter.getFormat() == b.getFormat())
                .filter(b -> filter.getMoodTags() == null || filter.getMoodTags().isEmpty()
                        || (b.getMoodTags() != null && b.getMoodTags().stream().anyMatch(filter.getMoodTags()::contains)))
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

    private Comparator<Book> comparatorFor(BookFilter filter) {
        Comparator<Book> comparator = switch (filter.getSortBy() == null ? "addedAt" : filter.getSortBy()) {
            case "title" -> Comparator.comparing(Book::getTitle, Comparator.nullsLast(String::compareToIgnoreCase));
            case "publicationYear" -> Comparator.comparing(Book::getPublicationYear, Comparator.nullsLast(Comparator.naturalOrder()));
            case "personalRating" -> Comparator.comparing(Book::getPersonalRating, Comparator.nullsLast(Comparator.naturalOrder()));
            case "pageCount" -> Comparator.comparing(Book::getPageCount, Comparator.nullsLast(Comparator.naturalOrder()));
            default -> Comparator.comparing(Book::getAddedAt, Comparator.nullsLast(Comparator.<Instant>naturalOrder()));
        };
        return filter.isSortDescending() ? comparator.reversed() : comparator;
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

    /** Distinct, sorted source strings the owner has already used — powers the Add Book source autocomplete. */
    public List<String> findDistinctSourcesByOwner(String ownerUid) throws ExecutionException, InterruptedException {
        QuerySnapshot snapshot = collection().whereEqualTo("ownerUid", ownerUid).get().get();
        return snapshot.getDocuments().stream()
                .map(doc -> doc.getString("source"))
                .filter(source -> source != null && !source.isBlank())
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
