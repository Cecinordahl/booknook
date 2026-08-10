package com.booknook.backend.controller;

import com.booknook.backend.dto.BookMetadataSuggestion;
import com.booknook.backend.exception.ResourceNotFoundException;
import com.booknook.backend.service.BookLookupService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/lookup")
public class LookupController {

    private final BookLookupService bookLookupService;

    public LookupController(BookLookupService bookLookupService) {
        this.bookLookupService = bookLookupService;
    }

    /** Used by both the barcode scanner and manual entry's "look up by ISBN" action. */
    @GetMapping("/isbn/{isbn}")
    public BookMetadataSuggestion lookupByIsbn(@PathVariable String isbn) {
        return bookLookupService.lookupByIsbn(isbn)
                .orElseThrow(() -> new ResourceNotFoundException("No match found for ISBN " + isbn));
    }

    /** Search-as-you-type for the Add Book title field. Empty list, not 404, when nothing matches. */
    @GetMapping("/search")
    public List<BookMetadataSuggestion> search(@RequestParam String q) {
        if (q.trim().length() < 3) {
            return List.of();
        }
        return bookLookupService.searchByTitle(q.trim());
    }
}
