package com.booknook.backend.exception;

/** A call to a third-party data source (Google Books, Hardcover, ...) failed outright — distinct
 * from that source legitimately returning zero matches. */
public class ExternalApiException extends RuntimeException {
    public ExternalApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
