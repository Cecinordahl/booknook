package com.booknook.backend.service;

import com.booknook.backend.dto.BookFilter;
import com.booknook.backend.dto.GoodreadsImportResult;
import com.booknook.backend.exception.ValidationException;
import com.booknook.backend.model.Book;
import com.booknook.backend.model.BookFormat;
import com.booknook.backend.model.BookStatus;
import com.booknook.backend.repository.BookRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Imports a library exported from Goodreads (goodreads.com > My Books > Import/Export > Export
 * Library — a CSV with columns like Title/Author/ISBN13/Exclusive Shelf/Binding). Column lookups
 * are case-insensitive so small header variations across Goodreads' export versions don't break
 * this outright; any row missing a usable title is skipped rather than failing the whole import.
 */
@Service
public class GoodreadsImportService {

    /** Goodreads' three built-in shelves — everything else is a user-created shelf/tag. */
    private static final Set<String> BUILT_IN_SHELVES = Set.of("read", "to-read", "currently-reading");

    private final BookService bookService;
    private final BookRepository bookRepository;

    public GoodreadsImportService(BookService bookService, BookRepository bookRepository) {
        this.bookService = bookService;
        this.bookRepository = bookRepository;
    }

    public GoodreadsImportResult importCsv(String ownerUid, MultipartFile file)
            throws IOException, ExecutionException, InterruptedException {
        if (file.isEmpty()) {
            throw new ValidationException("The uploaded file is empty.");
        }

        List<Book> existing = bookRepository.findByOwner(ownerUid, new BookFilter());
        Set<String> existingIsbns = new HashSet<>();
        Set<String> existingTitleAuthor = new HashSet<>();
        for (Book b : existing) {
            if (b.getIsbn() != null && !b.getIsbn().isBlank()) {
                existingIsbns.add(b.getIsbn());
            }
            existingTitleAuthor.add(dedupeKey(b.getTitle(), firstAuthor(b.getAuthors())));
        }

        List<Book> toCreate = new ArrayList<>();
        List<String> skippedTitles = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setIgnoreHeaderCase(true)
                .setTrim(true)
                .build();

        try (CSVParser parser = CSVParser.parse(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8), format)) {
            if (!parser.getHeaderNames().stream().anyMatch(h -> h.equalsIgnoreCase("Title"))) {
                throw new ValidationException(
                        "That doesn't look like a Goodreads export — no \"Title\" column found.");
            }

            for (CSVRecord record : parser) {
                try {
                    String title = field(record, "Title");
                    if (title == null) {
                        continue;
                    }

                    String author = field(record, "Author");
                    String isbn = firstNonBlank(
                            cleanIsbn(field(record, "ISBN13")), cleanIsbn(field(record, "ISBN")));
                    String dedupeKey = dedupeKey(title, author);

                    if ((isbn != null && existingIsbns.contains(isbn)) || existingTitleAuthor.contains(dedupeKey)) {
                        skippedTitles.add(title);
                        continue;
                    }

                    Book book = new Book();
                    book.setTitle(title);
                    book.setAuthors(authorsList(author, field(record, "Additional Authors")));
                    book.setIsbn(isbn);
                    book.setPageCount(parseInt(field(record, "Number of Pages")));
                    book.setPublicationYear(firstNonNull(
                            parseInt(field(record, "Original Publication Year")),
                            parseInt(field(record, "Year Published"))));
                    book.setPersonalRating(parseRating(field(record, "My Rating")));
                    book.setFormat(mapFormat(field(record, "Binding")));
                    book.setStatus(mapStatus(field(record, "Exclusive Shelf")));
                    book.setMoodTags(mapMoodTags(field(record, "Bookshelves")));

                    toCreate.add(book);
                    existingTitleAuthor.add(dedupeKey);
                    if (isbn != null) {
                        existingIsbns.add(isbn);
                    }
                } catch (Exception e) {
                    errors.add("Row " + record.getRecordNumber() + ": " + e.getMessage());
                }
            }
        }

        List<Book> created = bookService.bulkCreate(ownerUid, toCreate);
        return new GoodreadsImportResult(created.size(), skippedTitles.size(), skippedTitles, errors);
    }

    private String field(CSVRecord record, String name) {
        if (!record.isMapped(name)) {
            return null;
        }
        String value = record.get(name);
        return (value == null || value.isBlank()) ? null : value.trim();
    }

    /** Goodreads wraps ISBNs as ="0439023483" so Excel doesn't mangle leading zeros/long numbers. */
    private String cleanIsbn(String raw) {
        if (raw == null) {
            return null;
        }
        String cleaned = raw.replaceAll("^=\"|\"$", "").trim();
        return cleaned.isBlank() ? null : cleaned;
    }

    private Integer parseInt(String raw) {
        if (raw == null) {
            return null;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Goodreads uses 0 for "not rated" — that should stay unset, not become a 0-star rating. */
    private Double parseRating(String raw) {
        Integer rating = parseInt(raw);
        return (rating == null || rating == 0) ? null : rating.doubleValue();
    }

    private BookFormat mapFormat(String binding) {
        if (binding == null) {
            return BookFormat.PHYSICAL;
        }
        String b = binding.toLowerCase(Locale.ROOT);
        if (b.contains("audio")) {
            return BookFormat.AUDIOBOOK;
        }
        if (b.contains("kindle") || b.contains("ebook") || b.contains("e-book")) {
            return BookFormat.EBOOK;
        }
        return BookFormat.PHYSICAL;
    }

    private BookStatus mapStatus(String shelf) {
        if (shelf == null) {
            return BookStatus.NOT_STARTED;
        }
        return switch (shelf.toLowerCase(Locale.ROOT)) {
            case "read" -> BookStatus.FINISHED;
            case "currently-reading" -> BookStatus.READING;
            default -> BookStatus.NOT_STARTED;
        };
    }

    /** User-created shelves (not the three built-in ones) double as mood/theme tags. */
    private List<String> mapMoodTags(String shelves) {
        if (shelves == null) {
            return null;
        }
        List<String> tags = Arrays.stream(shelves.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty() && !BUILT_IN_SHELVES.contains(s.toLowerCase(Locale.ROOT)))
                .distinct()
                .collect(Collectors.toList());
        return tags.isEmpty() ? null : tags;
    }

    private List<String> authorsList(String primary, String additional) {
        List<String> authors = new ArrayList<>();
        if (primary != null) {
            authors.add(primary);
        }
        if (additional != null) {
            for (String a : additional.split(",")) {
                String trimmed = a.trim();
                if (!trimmed.isEmpty() && !authors.contains(trimmed)) {
                    authors.add(trimmed);
                }
            }
        }
        return authors;
    }

    private String dedupeKey(String title, String author) {
        String t = title == null ? "" : title.trim().toLowerCase(Locale.ROOT);
        String a = author == null ? "" : author.trim().toLowerCase(Locale.ROOT);
        return t + "|" + a;
    }

    private String firstAuthor(List<String> authors) {
        return (authors == null || authors.isEmpty()) ? null : authors.get(0);
    }

    private static String firstNonBlank(String a, String b) {
        return (a != null && !a.isBlank()) ? a : b;
    }

    private static Integer firstNonNull(Integer a, Integer b) {
        return a != null ? a : b;
    }
}
