package com.booknook.backend.model;

import java.time.Instant;

/**
 * A single invited email address. Document ID in Firestore is the lowercased email itself, so
 * membership checks are a direct document lookup rather than a query.
 */
public class AllowlistEntry {

    private String email;
    private String addedBy;
    private Instant addedAt;

    public AllowlistEntry() {
    }

    public AllowlistEntry(String email, String addedBy, Instant addedAt) {
        this.email = email;
        this.addedBy = addedBy;
        this.addedAt = addedAt;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddedBy() {
        return addedBy;
    }

    public void setAddedBy(String addedBy) {
        this.addedBy = addedBy;
    }

    public Instant getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(Instant addedAt) {
        this.addedAt = addedAt;
    }
}
