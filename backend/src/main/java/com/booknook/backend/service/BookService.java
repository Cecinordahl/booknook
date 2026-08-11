package com.booknook.backend.service;

import com.booknook.backend.dto.BookFilter;
import com.booknook.backend.dto.HardcoverBookSeriesMatch;
import com.booknook.backend.exception.ForbiddenException;
import com.booknook.backend.exception.ResourceNotFoundException;
import com.booknook.backend.model.Book;
import com.booknook.backend.model.Series;
import com.booknook.backend.repository.BookRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Service
public class BookService {

    private static final Logger log = LoggerFactory.getLogger(BookService.class);

    private final BookRepository bookRepository;
    private final BookLookupService bookLookupService;
    private final SeriesService seriesService;

    public BookService(BookRepository bookRepository, BookLookupService bookLookupService, SeriesService seriesService) {
        this.bookRepository = bookRepository;
        this.bookLookupService = bookLookupService;
        this.seriesService = seriesService;
    }

    public List<Book> list(String ownerUid, BookFilter filter) throws ExecutionException, InterruptedException {
        return bookRepository.findByOwner(ownerUid, filter);
    }

    public List<String> listGenres(String ownerUid) throws ExecutionException, InterruptedException {
        return bookRepository.findDistinctGenresByOwner(ownerUid);
    }

    public Book get(String ownerUid, String bookId) throws ExecutionException, InterruptedException {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found: " + bookId));
        requireOwner(ownerUid, book);
        return book;
    }

    public Book create(String ownerUid, Book book) throws ExecutionException, InterruptedException {
        book.setId(null);
        book.setOwnerUid(ownerUid);
        Instant now = Instant.now();
        book.setAddedAt(now);
        book.setUpdatedAt(now);

        attachSeriesIfKnown(ownerUid, book);

        return bookRepository.save(book);
    }

    /**
     * Best-effort: if this book has an ISBN, check whether Hardcover knows what series it
     * belongs to and auto-follow that series. Never fails book creation — a Hardcover lookup
     * problem here is a missed enrichment, not a reason to lose the user's book.
     */
    private void attachSeriesIfKnown(String ownerUid, Book book) {
        if (book.getIsbn() == null || book.getIsbn().isBlank()) {
            return;
        }
        try {
            Optional<HardcoverBookSeriesMatch> match = bookLookupService.findSeriesForIsbn(book.getIsbn());
            if (match.isEmpty()) {
                return;
            }
            Series series = seriesService.autoAttachSeriesForBook(
                    ownerUid, match.get().hardcoverSeriesId(), match.get().seriesName());
            book.setSeriesId(series.getId());
            book.setSeriesPosition(match.get().position());
        } catch (Exception e) {
            log.warn("Could not auto-attach series for ISBN {}: {}", book.getIsbn(), e.getMessage());
        }
    }

    public Book update(String ownerUid, String bookId, Book updates) throws ExecutionException, InterruptedException {
        Book existing = get(ownerUid, bookId);

        existing.setTitle(updates.getTitle());
        existing.setAuthors(updates.getAuthors());
        existing.setIsbn(updates.getIsbn());
        existing.setCoverImageUrl(updates.getCoverImageUrl());
        existing.setPageCount(updates.getPageCount());
        existing.setCurrentPage(updates.getCurrentPage());
        existing.setFormat(updates.getFormat());
        existing.setStatus(updates.getStatus());
        existing.setGenre(updates.getGenre());
        existing.setMoodTags(updates.getMoodTags());
        existing.setPublicationYear(updates.getPublicationYear());
        existing.setPersonalRating(updates.getPersonalRating());
        existing.setSeriesId(updates.getSeriesId());
        existing.setSeriesPosition(updates.getSeriesPosition());
        existing.setHardcoverBookId(updates.getHardcoverBookId());
        existing.setUpdatedAt(Instant.now());

        return bookRepository.save(existing);
    }

    public void delete(String ownerUid, String bookId) throws ExecutionException, InterruptedException {
        Book existing = get(ownerUid, bookId);
        bookRepository.deleteById(existing.getId());
    }

    private void requireOwner(String ownerUid, Book book) {
        if (!book.getOwnerUid().equals(ownerUid)) {
            throw new ForbiddenException("You do not own this book.");
        }
    }
}
