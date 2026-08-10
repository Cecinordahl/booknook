package com.booknook.backend.service;

import com.booknook.backend.dto.BookFilter;
import com.booknook.backend.exception.ForbiddenException;
import com.booknook.backend.exception.ResourceNotFoundException;
import com.booknook.backend.model.Book;
import com.booknook.backend.repository.BookRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
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
        return bookRepository.save(book);
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
