package com.smartcampus.exception;

/**
 * Part 5.3 - Thrown when a POST reading is attempted on a sensor in MAINTENANCE status.
 * Mapped to HTTP 403 Forbidden by SensorUnavailableExceptionMapper.
 *
 * A MAINTENANCE sensor is physically disconnected and cannot accept new data.
 * 403 Forbidden is appropriate because the server understands the request
 * but refuses to process it due to the current state of the resource.
 */
public class SensorUnavailableException extends RuntimeException {
    public SensorUnavailableException(String message) {
        super(message);
    }
}
