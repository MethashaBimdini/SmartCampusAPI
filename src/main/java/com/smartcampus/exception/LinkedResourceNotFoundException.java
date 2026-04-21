package com.smartcampus.exception;

/**
 * Part 5.2 - Thrown when a POST /sensors references a roomId that does not exist.
 * Mapped to HTTP 422 Unprocessable Entity by LinkedResourceNotFoundExceptionMapper.
 *
 * 422 is more semantically accurate than 404 here because:
 * - 404 means the requested resource URL was not found
 * - 422 means the request was well-formed but contained invalid semantic content
 *   (the JSON payload is valid, but a referenced entity inside it doesn't exist)
 */
public class LinkedResourceNotFoundException extends RuntimeException {
    public LinkedResourceNotFoundException(String message) {
        super(message);
    }
}
