package com.smartcampus.exception;

import com.smartcampus.model.ErrorResponse;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Maps JAX-RS NotFoundException to a clean JSON 404 response.
 * Prevents Jersey's default HTML error page from leaking to API consumers.
 */
@Provider
public class NotFoundExceptionMapper implements ExceptionMapper<NotFoundException> {

    @Override
    public Response toResponse(NotFoundException exception) {
        ErrorResponse error = new ErrorResponse(
            404,
            "Not Found",
            exception.getMessage() != null ? exception.getMessage() : "The requested resource was not found."
        );
        return Response
            .status(Response.Status.NOT_FOUND)
            .entity(error)
            .type(MediaType.APPLICATION_JSON)
            .build();
    }
}
