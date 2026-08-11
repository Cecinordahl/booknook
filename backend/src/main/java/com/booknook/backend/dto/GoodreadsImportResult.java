package com.booknook.backend.dto;

import java.util.List;

/** Summary returned after processing a Goodreads library-export CSV. */
public class GoodreadsImportResult {

    private int imported;
    private int skipped;
    private List<String> skippedTitles;
    private List<String> errors;

    public GoodreadsImportResult() {
    }

    public GoodreadsImportResult(int imported, int skipped, List<String> skippedTitles, List<String> errors) {
        this.imported = imported;
        this.skipped = skipped;
        this.skippedTitles = skippedTitles;
        this.errors = errors;
    }

    public int getImported() {
        return imported;
    }

    public void setImported(int imported) {
        this.imported = imported;
    }

    public int getSkipped() {
        return skipped;
    }

    public void setSkipped(int skipped) {
        this.skipped = skipped;
    }

    public List<String> getSkippedTitles() {
        return skippedTitles;
    }

    public void setSkippedTitles(List<String> skippedTitles) {
        this.skippedTitles = skippedTitles;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }
}
