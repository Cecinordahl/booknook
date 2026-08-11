package com.booknook.backend.exception;

/** Request input failed a business-rule check (as opposed to malformed JSON, which Spring rejects earlier). */
public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}
