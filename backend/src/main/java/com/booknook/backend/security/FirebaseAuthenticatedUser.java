package com.booknook.backend.security;

/**
 * The authenticated principal attached to the security context once a request's Firebase ID
 * token has been verified and its email confirmed against the allowlist.
 */
public record FirebaseAuthenticatedUser(String uid, String email, String displayName) {
}
