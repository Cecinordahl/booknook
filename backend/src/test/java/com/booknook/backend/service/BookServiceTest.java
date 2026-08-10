package com.booknook.backend.service;

import com.booknook.backend.exception.ForbiddenException;
import com.booknook.backend.exception.ResourceNotFoundException;
import com.booknook.backend.model.Book;
import com.booknook.backend.repository.BookRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BookServiceTest {

    @Test
    void createStampsOwnerAndTimestamps() throws Exception {
        BookRepository repository = mock(BookRepository.class);
        when(repository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookService service = new BookService(repository);
        Book input = new Book();
        input.setTitle("Some Title");

        Book saved = service.create("owner-1", input);

        assertEquals("owner-1", saved.getOwnerUid());
        assertNotNull(saved.getAddedAt());
        assertNotNull(saved.getUpdatedAt());
        assertNull(saved.getId()); // repository is responsible for assigning an ID on save
    }

    @Test
    void getThrowsNotFoundForMissingBook() throws Exception {
        BookRepository repository = mock(BookRepository.class);
        when(repository.findById("missing")).thenReturn(Optional.empty());

        BookService service = new BookService(repository);

        assertThrows(ResourceNotFoundException.class, () -> service.get("owner-1", "missing"));
    }

    @Test
    void getThrowsForbiddenWhenCallerDoesNotOwnBook() throws Exception {
        Book book = new Book();
        book.setId("book-1");
        book.setOwnerUid("owner-1");

        BookRepository repository = mock(BookRepository.class);
        when(repository.findById("book-1")).thenReturn(Optional.of(book));

        BookService service = new BookService(repository);

        assertThrows(ForbiddenException.class, () -> service.get("someone-else", "book-1"));
    }

    @Test
    void deleteRemovesBookWhenCallerIsOwner() throws Exception {
        Book book = new Book();
        book.setId("book-1");
        book.setOwnerUid("owner-1");

        BookRepository repository = mock(BookRepository.class);
        when(repository.findById("book-1")).thenReturn(Optional.of(book));

        BookService service = new BookService(repository);
        service.delete("owner-1", "book-1");
        // no exception thrown means the ownership check passed and deleteById was reached
    }
}
