package com.booknook.backend.future;

/**
 * INTENTIONALLY NOT IMPLEMENTED.
 *
 * <p>Auto-syncing "what I'm currently reading/listening to" from Audible or the Kindle Store was
 * considered for this app and explicitly deferred. Amazon does not offer a public API for this.
 * Any working solution would mean reverse-engineering Audible/Kindle's private mobile-app APIs
 * and/or scraping account pages — both violate Amazon's Terms of Service, and are fragile (breaks
 * on any endpoint change) and risky (potential account suspension for the user whose credentials
 * are used).
 *
 * <p>Booknook's outbound links to Audible/Kindle (see {@code OutboundLinks} on the frontend) are
 * the supported alternative: they open a search for the book on those storefronts rather than
 * touching the user's account. If auto-sync is revisited later, it should be scoped as a
 * deliberate, opt-in, clearly-risk-flagged feature — not something that happens by default.
 */
public final class AudibleSyncStub {
    private AudibleSyncStub() {
    }
}
