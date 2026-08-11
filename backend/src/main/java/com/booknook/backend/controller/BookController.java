package com.booknook.backend.controller;

import com.booknook.backend.dto.BookFilter;
import com.booknook.backend.dto.GoodreadsImportResult;
import com.booknook.backend.model.Book;
import com.booknook.backend.security.FirebaseAuthenticatedUser;
import com.booknook.backend.service.BookService;
import com.booknook.backend.service.GoodreadsImportService;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;
    private final GoodreadsImportService goodreadsImportService;

    public BookController(BookService bookService, GoodreadsImportService goodreadsImportService) {
        this.bookService = bookService;
        this.goodreadsImportService = goodreadsImportService;
    }

    @GetMapping
    public List<Book> list(@AuthenticationPrincipal FirebaseAuthenticatedUser principal, BookFilter filter)
            throws ExecutionException, InterruptedException {
        return bookService.list(principal.uid(), filter);
    }

    /** Distinct genres the caller has already used, for the Add Book genre autocomplete. */
    @GetMapping("/genres")
    public List<String> genres(@AuthenticationPrincipal FirebaseAuthenticatedUser principal)
            throws ExecutionException, InterruptedException {
        return bookService.listGenres(principal.uid());
    }

    /** Distinct sources the caller has already used, for the Add Book source autocomplete. */
    @GetMapping("/sources")
    public List<String> sources(@AuthenticationPrincipal FirebaseAuthenticatedUser principal)
            throws ExecutionException, InterruptedException {
        return bookService.listSources(principal.uid());
    }

    @GetMapping("/{id}")
    public Book get(@AuthenticationPrincipal FirebaseAuthenticatedUser principal, @PathVariable String id)
            throws ExecutionException, InterruptedException {
        return bookService.get(principal.uid(), id);
    }

    @PostMapping
    public Book create(@AuthenticationPrincipal FirebaseAuthenticatedUser principal, @RequestBody Book book)
            throws ExecutionException, InterruptedException {
        return bookService.create(principal.uid(), book);
    }

    @PutMapping("/{id}")
    public Book update(@AuthenticationPrincipal FirebaseAuthenticatedUser principal, @PathVariable String id,
                        @RequestBody Book book) throws ExecutionException, InterruptedException {
        return bookService.update(principal.uid(), id, book);
    }

    @DeleteMapping("/{id}")
    public void delete(@AuthenticationPrincipal FirebaseAuthenticatedUser principal, @PathVariable String id)
            throws ExecutionException, InterruptedException {
        bookService.delete(principal.uid(), id);
    }

    @PostMapping(value = "/import/goodreads", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public GoodreadsImportResult importGoodreads(@AuthenticationPrincipal FirebaseAuthenticatedUser principal,
                                                  @RequestParam("file") MultipartFile file)
            throws ExecutionException, InterruptedException, IOException {
        return goodreadsImportService.importCsv(principal.uid(), file);
    }
}
