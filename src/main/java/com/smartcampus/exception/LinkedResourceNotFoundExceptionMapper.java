package com.smartcampus.exception;

import com.smartcampus.model.ErrorResponse;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Part 5.2 - Exception Mapper for LinkedResourceNotFoundException
 *
 * Returns HTTP 422 Unprocessable Entity when a sensor registration references
 * a roomId that does not exist in the system.
 *
 * Why 422 over 404?
 * - 404 Not Found: The URL path itself could not be found on the server.
 * - 422 Unprocessable Entity: The request URL is valid and the JSON body is
 *   syntactically correct, but the semantic content is invalid — a referenced
 *   entity (roomId) inside the payload does not exist. The server understood
 *   the request perfectly but cannot process it due to this dependency violation.
 */
@Provider
public class LinkedResourceNotFoundExceptionMapper implements ExceptionMapper<LinkedResourceNotFoundException> {

    @Override
    public Response toResponse(LinkedResourceNotFoundException exception) {
        ErrorResponse error = new ErrorResponse(
            422,
            "Unprocessable Entity",
            exception.getMessage()
        );
        return Response
            .status(422)
            .entity(error)
            .type(MediaType.APPLICATION_JSON)
            .build();
    }
}
