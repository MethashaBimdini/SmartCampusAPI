package com.smartcampus.exception;

import com.smartcampus.model.ErrorResponse;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Part 5.3 - Exception Mapper for SensorUnavailableException
 *
 * Returns HTTP 403 Forbidden when a POST reading is attempted on a sensor
 * that is in MAINTENANCE or OFFLINE status.
 *
 * 403 Forbidden is the correct code here because:
 * - The server fully understood the request
 * - The client is authenticated (no auth issue)
 * - The server refuses to fulfil the request due to the resource's current STATE
 */
@Provider
public class SensorUnavailableExceptionMapper implements ExceptionMapper<SensorUnavailableException> {

    @Override
    public Response toResponse(SensorUnavailableException exception) {
        ErrorResponse error = new ErrorResponse(
            403,
            "Forbidden",
            exception.getMessage()
        );
        return Response
            .status(Response.Status.FORBIDDEN)
            .entity(error)
            .type(MediaType.APPLICATION_JSON)
            .build();
    }
}
