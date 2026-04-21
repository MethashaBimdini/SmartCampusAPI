package com.smartcampus.exception;

/**
 * Part 5.1 - Thrown when attempting to delete a Room that still has Sensors.
 * Mapped to HTTP 409 Conflict by RoomNotEmptyExceptionMapper.
 */
public class RoomNotEmptyException extends RuntimeException {
    public RoomNotEmptyException(String message) {
        super(message);
    }
}
