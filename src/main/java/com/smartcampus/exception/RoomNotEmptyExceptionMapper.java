package com.smartcampus.exception;

import com.smartcampus.model.ErrorResponse;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Part 5.1 - Exception Mapper for RoomNotEmptyException
 *
 * Returns HTTP 409 Conflict with a structured JSON error body when a client
 * attempts to delete a Room that still has Sensors assigned to it.
 *
 * The @Provider annotation registers this mapper with the JAX-RS runtime.
 */
@Provider
public class RoomNotEmptyExceptionMapper implements ExceptionMapper<RoomNotEmptyException> {

    @Override
    public Response toResponse(RoomNotEmptyException exception) {
        ErrorResponse error = new ErrorResponse(
            409,
            "Conflict",
            exception.getMessage()
        );
        return Response
            .status(Response.Status.CONFLICT)
            .entity(error)
            .type(MediaType.APPLICATION_JSON)
            .build();
    }
}
